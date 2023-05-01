package com.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.core.GameConstants;
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
        public Vector2 position = new Vector2();
        public DrawComponent draw = new DrawComponent();
    }

    private Pool<Renderable> renderablePool = new Pool<Renderable>(4096)
    {
        @Override
        protected Renderable newObject()
        {
            return new Renderable();
        }
    };

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

        renderable.position.x = p.position.x * alpha + p.previousPosition.x * (1.0f - alpha);
        renderable.position.y = p.position.y * alpha + p.previousPosition.y * (1.0f - alpha);
        renderable.draw = d;


        renderables.add(renderable);
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

        sb.end();

        renderablePool.freeAll(renderables);
        renderables.clear();
    }
}
