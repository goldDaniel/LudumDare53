package com.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.core.GameConstants;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.PhysicsComponent;
import com.ecs.components.PositionComponent;
import com.ecs.components.TagComponent;
import com.ecs.events.AddCameraZoneEvent;
import com.ecs.events.CameraUpdateEvent;
import com.ecs.events.Event;

public class CameraUpdateSystem extends System
{
    private Camera cam;

    private ComponentMapper<TagComponent> tagMapper = ComponentMapper.getFor(TagComponent.class);
    private ComponentMapper<PositionComponent> posMapper = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<PhysicsComponent> physMapper = ComponentMapper.getFor(PhysicsComponent.class);

    Rectangle currentZone = null;
    private Array<Rectangle> cameraZones = new Array<>();

    public CameraUpdateSystem(Engine engine)
    {
        super(engine);
        cam = new OrthographicCamera(GameConstants.CAMERA_DIMENSIONS,GameConstants.CAMERA_DIMENSIONS);

        registerComponentType(TagComponent.class);
        registerComponentType(PhysicsComponent.class);
        registerComponentType(PositionComponent.class);

        registerEventType(AddCameraZoneEvent.class);
    }

    @Override
    protected void updateEntity(Entity entity, float alpha)
    {
        if(tagMapper.get(entity).tag.equals("player"))
        {
            PositionComponent p = posMapper.get(entity);
            PhysicsComponent phys = physMapper.get(entity);

            float desiredX = p.position.x * alpha + p.previousPosition.x * (1.0f - alpha) - MathUtils.cos(phys.body.getAngle()) * MathUtils.clamp(phys.body.getLinearVelocity().x / 30.f, -2, 2);
            float desiredY = p.position.y * alpha + p.previousPosition.y * (1.0f - alpha) - MathUtils.sin(phys.body.getAngle()) * MathUtils.clamp(phys.body.getLinearVelocity().y / 30.f, -2, 2);

            cam.position.x = MathUtils.lerp(cam.position.x, desiredX, 0.25f);
            cam.position.y = MathUtils.lerp(cam.position.y, desiredY, 0.25f);

            cam.update();

            engine.fireEvent(new CameraUpdateEvent(entity, cam));
        }
    }

    @Override
    protected void handleEvent(Event event)
    {
        if(event instanceof AddCameraZoneEvent)
        {
            AddCameraZoneEvent zone = (AddCameraZoneEvent)event;
            cameraZones.add(new Rectangle(zone.x, zone.y, zone.w, zone.h));
        }
    }
}
