package com.ecs.systems;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.PositionComponent;
import com.ecs.components.TagComponent;
import com.ecs.events.CameraUpdateEvent;

public class CameraUpdateSystem extends System
{
    private Camera cam;

    public CameraUpdateSystem(Engine engine)
    {
        super(engine);
        cam = new OrthographicCamera(16,16);

        registerComponentType(TagComponent.class);
        registerComponentType(PositionComponent.class);
    }

    @Override
    protected void updateEntity(Entity entity, float alpha)
    {
        if(entity.getComponent(TagComponent.class).tag.equals("player"))
        {
            PositionComponent p = entity.getComponent(PositionComponent.class);

            cam.position.x = p.position.x * alpha + p.previousPosition.x * (1.0f - alpha);
            cam.position.y = p.position.y * alpha + p.previousPosition.y * (1.0f - alpha);
            cam.update();

            engine.fireEvent(new CameraUpdateEvent(entity, cam));
        }
    }
}
