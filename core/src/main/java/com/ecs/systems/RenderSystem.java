package com.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.DrawComponent;
import com.ecs.components.PositionComponent;
import com.ecs.events.CameraUpdateEvent;
import com.ecs.events.Event;
import com.ecs.events.ResizeEvent;

public class RenderSystem extends System
{
    private class Renderable
    {
        final Vector2 position;
        final DrawComponent draw;

        public Renderable(Vector2 position, DrawComponent d)
        {
            this.position = position;
            this.draw = d;
        }
    }

    private final Array<Renderable> renderables = new Array<>();

    private Viewport viewport = new ExtendViewport(32,32);

    private final SpriteBatch sb;

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
        PositionComponent p = entity.getComponent(PositionComponent.class);
        DrawComponent d = entity.getComponent(DrawComponent.class);

        Vector2 position = new Vector2();
        position.x = p.position.x * alpha + p.previousPosition.x * (1.0f - alpha);
        position.y = p.position.y * alpha + p.previousPosition.y * (1.0f - alpha);

        renderables.add(new Renderable(position, d));
    }

    @Override
    public void postUpdate()
    {
        sb.begin();

        for(Renderable r : renderables)
        {
            Vector2 pos = r.position;
            DrawComponent d = r.draw;

            sb.setColor(d.currentColor);

            float width = d.scale.x;
            float height = d.scale.y;

            boolean facingLeft = d.facingLeft;

            if(d.rotation > 90 && d.rotation < 270)
            {
                facingLeft = !facingLeft;
            }

            if(facingLeft)
            {
                sb.draw(new TextureRegion(d.texture),
                    pos.x - width / 2f, pos.y - height / 2f,
                    width / 2, height / 2,
                    width, height,
                    1f, -1f,
                    d.rotation);
            }
            else
            {
                sb.draw(new TextureRegion(d.texture),
                    pos.x - width / 2f, pos.y - height / 2f,
                    width / 2, height / 2,
                     width, height,
                    1f, 1f,
                    d.rotation);
            }
        }

        sb.end();
    }
}
