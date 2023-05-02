package com.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.core.GameConstants;
import com.core.RenderResources;
import com.ecs.Component;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.DrawComponent;
import com.ecs.components.PositionComponent;
import com.ecs.events.BombEvent;
import com.ecs.events.CameraUpdateEvent;
import com.ecs.events.Event;
import com.ecs.events.ResizeEvent;

import java.util.Comparator;

public class RenderSystem extends System
{
    private class Renderable
    {
        public final Vector2 position = new Vector2();
        public final DrawComponent draw = new DrawComponent();
    }

    private Pool<Renderable> renderablePool = new Pool<Renderable>(4096)
    {
        @Override
        protected Renderable newObject()
        {
            return new Renderable();
        }

        @Override
        protected void reset(Renderable r)
        {
            r.position.setZero();
        }
    };

    private Pool<Vector2> vectorPool = new Pool<Vector2>(16)
    {
        @Override
        protected Vector2 newObject()
        {
            return new Vector2();
        }

        @Override
        protected void reset(Vector2 object)
        {
            object.setZero();
        }
    };
    private Rectangle camRect = new Rectangle();
    private Rectangle aabbRect = new Rectangle();


    private final Array<Renderable> tiles = new Array<>();
    private final Array<Renderable> renderables = new Array<>();

    private Viewport viewport = new ExtendViewport(GameConstants.CAMERA_DIMENSIONS,GameConstants.CAMERA_DIMENSIONS);

    private final SpriteBatch sb;

    private ComponentMapper<PositionComponent> posMapper = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<DrawComponent> drawMapper = ComponentMapper.getFor(DrawComponent.class);

    private Array<TextureRegion> explosion = new Array<>();

    private Array<Texture> backgrounds = new Array<>();

    class AnimExplosion
    {
        public Vector2 pos = new Vector2();
        float stateTime = 0;
        public Animation<TextureRegion> animation = new Animation<>(0.016f, explosion, Animation.PlayMode.LOOP);
    }

    private Array<AnimExplosion> animations = new Array<>();

    public RenderSystem(Engine engine, SpriteBatch sb)
    {
        super(engine);
        this.sb = sb;

        registerComponentType(PositionComponent.class);
        registerComponentType(DrawComponent.class);

        registerEventType(ResizeEvent.class);
        registerEventType(CameraUpdateEvent.class);
        registerEventType(BombEvent.class);

        for(int i = 0; i <= 7; i++)
        {
            backgrounds.add(RenderResources.getTexture("textures/background/" + i + ".png"));
        }

        Texture explosionTex = RenderResources.getTexture("textures/entities/explosion.png");
        for(int i = 0; i < 4; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                int regionX = j * 64;
                int regionY = i * 64;

                int regionW = 64;
                int regionH = 64;

                explosion.add(new TextureRegion(explosionTex, regionX,regionY, regionW, regionH));
            }
        }
    }

    public void submitTile(float x, float y, float width, float height, TextureRegion region)
    {
        Renderable tile = renderablePool.obtain();

        tile.position.set(x, y);

        tile.draw.texture.setTexture(region.getTexture());
        tile.draw.texture.setRegionX(region.getRegionX());
        tile.draw.texture.setRegionY(region.getRegionY());
        tile.draw.texture.setRegionWidth(region.getRegionWidth());
        tile.draw.texture.setRegionHeight(region.getRegionHeight());

        tile.draw.scale.set(width, height);
        tile.draw.flipX = false;
        tile.draw.flipY = false;
        tile.draw.rotation = 0;
        tile.draw.currentColor.set(Color.WHITE);

        tiles.add(tile);
    }

    protected void handleEvent(Event event)
    {
        if(event instanceof ResizeEvent)
        {
            ResizeEvent e = (ResizeEvent)event;
            viewport.update(e.width, e.height);
            viewport.apply();
        }
        else if(event instanceof CameraUpdateEvent)
        {
            CameraUpdateEvent e = (CameraUpdateEvent)event;
            viewport.setCamera(e.cam);
            viewport.apply();
        }
        else if(event instanceof BombEvent)
        {
            BombEvent b = (BombEvent)event;

            AnimExplosion ex = new AnimExplosion();
            ex.pos.set(b.pos);

            animations.add(ex);
        }
    }

    @Override
    public void preUpdate()
    {
        renderables.clear();
    }

    @Override
    public void updateEntity(Entity entity, float alpha)
    {
        PositionComponent p = posMapper.get(entity);
        DrawComponent d = drawMapper.get(entity);

        Renderable renderable = renderablePool.obtain();

        //if(!frustumCull(p.position, d.scale) || !frustumCull(p.previousPosition, d.scale))
        {
            renderable.position.x = p.position.x * alpha + p.previousPosition.x * (1.0f - alpha);
            renderable.position.y = p.position.y * alpha + p.previousPosition.y * (1.0f - alpha);

            if(d.callback != null)
            {
                d.callback.execute(d.texture, 0.001f);
            }

            renderable.draw.texture.setRegion(d.texture);
            renderable.draw.scale.set(d.scale);
            renderable.draw.flipX = d.flipX;
            renderable.draw.flipY = d.flipY;
            renderable.draw.rotation = d.rotation;
            renderable.draw.currentColor.set(d.currentColor);

            renderables.add(renderable);
        }
    }

    @Override
    public void postUpdate()
    {
        sb.setProjectionMatrix(new Matrix4().idt());
        sb.begin();
        for (Texture bg : backgrounds)
        {
            sb.draw(bg, -1f, -1f, 2f, 2f);
        }
        sb.end();


        renderables.sort(Comparator.comparingInt(r -> r.draw.texture.getTexture().glTarget));
        sb.setProjectionMatrix(viewport.getCamera().combined);
        sb.begin();

        Array<AnimExplosion> toRemove = new Array<>();
        for(AnimExplosion e : animations)
        {
            if(e.animation.isAnimationFinished(e.stateTime))
            {
                toRemove.add(e);
            }
            else
            {
                sb.draw(e.animation.getKeyFrame(e.stateTime), e.pos.x, e.pos.y, 1f,1f);
                e.stateTime += 0.008f;
            }
        }
        animations.removeAll(toRemove, true);

        for(Renderable r : renderables)
        {
            Vector2 pos = r.position;
            DrawComponent d = r.draw;

            sb.setColor(d.currentColor);

            float width = d.scale.x;
            float height = d.scale.y;

            float scaleX = d.flipX ? -1 : 1;
            float scaleY = d.flipY ? -1 : 1;

            sb.draw(d.texture,
                pos.x - width / 2f, pos.y - height / 2f,
                width / 2, height / 2,
                width, height,
                scaleX, scaleY,
                d.rotation);
        }

        for(Renderable tile : tiles)
        {
            Vector2 pos = tile.position;
            DrawComponent d = tile.draw;
            if(!frustumCull(pos, d.scale))
            {
                float width = d.scale.x;
                float height = d.scale.y;
                sb.draw(d.texture, pos.x - width / 2, pos.y - height / 2, width, height);
            }
        }

        sb.end();

        renderablePool.freeAll(renderables);
    }

    private boolean frustumCull(Vector2 p, Vector2 size)
    {
        float camRadius = Math.max(viewport.getCamera().viewportWidth, viewport.getCamera().viewportHeight) * 1.414f;
        float objRadius = Math.max(size.x, size.y) * 1.414f;

        float dX = p.x - viewport.getCamera().position.x;
        dX *= dX;

        float dY = p.y - viewport.getCamera().position.y;
        dY *= dY;

        float r2 = (camRadius + objRadius);
        r2 *= r2;

        return !(dX + dY <= r2);
    }
}
