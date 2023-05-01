package com.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
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

    public RenderSystem(Engine engine, SpriteBatch sb)
    {
        super(engine);
        this.sb = sb;

        registerComponentType(PositionComponent.class);
        registerComponentType(DrawComponent.class);

        registerEventType(ResizeEvent.class);
        registerEventType(CameraUpdateEvent.class);
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
    }

    @Override
    public void preUpdate()
    {
        renderables.clear();
        sb.setProjectionMatrix(viewport.getCamera().combined);
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
        renderables.sort(Comparator.comparingInt(r -> r.draw.texture.getTexture().glTarget));
        sb.begin();

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

            float width = d.scale.x;
            float height = d.scale.y;

            sb.draw(d.texture, pos.x - width / 2, pos.y - height / 2, width, height);
        }

        sb.end();

        renderablePool.freeAll(renderables);
    }

    private boolean frustumCull(Vector2 p, Vector2 size)
    {

        float frustumBuffer = 1;

        float viewWidth = viewport.getCamera().viewportWidth + frustumBuffer;
        float viewHeight = viewport.getCamera().viewportHeight + frustumBuffer;

        camRect.setPosition(viewport.getCamera().position.x - viewWidth / 2, viewport.getCamera().position.y - viewHeight / 2);
        camRect.setSize(viewWidth, viewHeight);

        Vector2 p0 = vectorPool.obtain();
        Vector2 p1 = vectorPool.obtain();
        Vector2 p2 = vectorPool.obtain();
        Vector2 p3 = vectorPool.obtain();

        p0.set(p).add(size.x / 2.0f, size.y / 2.0f);
        p1.set(p).add(-size.x / 2.0f, -size.y / 2.0f);
        p2.set(p).add(size.x / 2.0f, -size.y / 2.0f);
        p3.set(p).add(-size.x / 2.0f, size.y / 2.0f);


        Vector2 min = vectorPool.obtain().set(Float.MAX_VALUE, Float.MAX_VALUE);
        Vector2 max = vectorPool.obtain().set(Float.MIN_VALUE, Float.MIN_VALUE);

        if(p0.x < min.x) min.x = p0.x;
        if(p1.x < min.x) min.x = p1.x;
        if(p2.x < min.x) min.x = p2.x;
        if(p3.x < min.x) min.x = p3.x;

        if(p0.y < min.y) min.y = p0.y;
        if(p1.y < min.y) min.y = p1.y;
        if(p2.y < min.y) min.y = p2.y;
        if(p3.y < min.y) min.y = p3.y;

        if(p0.x > max.x) max.x = p0.x;
        if(p1.x > max.x) max.x = p1.x;
        if(p2.x > max.x) max.x = p2.x;
        if(p3.x > max.x) max.x = p3.x;

        if(p0.y > max.y) max.y = p0.y;
        if(p1.y > max.y) max.y = p1.y;
        if(p2.y > max.y) max.y = p2.y;
        if(p3.y > max.y) max.y = p3.y;

        float width = Math.abs(max.x - min.x);
        float height = Math.abs(max.x - min.x);

        aabbRect.set(min.x, min.y, width, height);

        boolean result = !camRect.overlaps(aabbRect);

        vectorPool.free(p0);
        vectorPool.free(p1);
        vectorPool.free(p2);
        vectorPool.free(p3);

        vectorPool.free(min);
        vectorPool.free(max);


        return result;
    }
}
