package com.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.core.GameConstants;
import com.ecs.Component;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.PositionComponent;
import com.ecs.components.TagComponent;
import com.ecs.events.CameraUpdateEvent;

public class CameraUpdateSystem extends System
{
    private Camera cam;

    private ComponentMapper<TagComponent> tagMapper = ComponentMapper.getFor(TagComponent.class);
    private ComponentMapper<PositionComponent> posMapper = ComponentMapper.getFor(PositionComponent.class);

    public CameraUpdateSystem(Engine engine)
    {
        super(engine);
        cam = new OrthographicCamera(GameConstants.CAMERA_DIMENSIONS,GameConstants.CAMERA_DIMENSIONS);

        registerComponentType(TagComponent.class);
        registerComponentType(PositionComponent.class);
    }

    @Override
    protected void updateEntity(Entity entity, float alpha)
    {
        if(tagMapper.get(entity).tag.equals("player"))
        {
            PositionComponent p = posMapper.get(entity);

            cam.position.x = p.position.x * alpha + p.previousPosition.x * (1.0f - alpha);
            cam.position.y = p.position.y * alpha + p.previousPosition.y * (1.0f - alpha);
            cam.update();

            engine.fireEvent(new CameraUpdateEvent(entity, cam));
        }
    }
}
