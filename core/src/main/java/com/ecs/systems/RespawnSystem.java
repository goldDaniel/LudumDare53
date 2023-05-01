package com.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.PhysicsComponent;
import com.ecs.components.PositionComponent;
import com.ecs.components.RespawnComponent;
import com.ecs.events.CheckpointEvent;
import com.ecs.events.Event;

public class RespawnSystem extends System
{
    private final Vector2 currentRespawnCoordinate = new Vector2();

    private ComponentMapper<PositionComponent> posMapper = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<PhysicsComponent> physMapper = ComponentMapper.getFor(PhysicsComponent.class);

    public RespawnSystem(Engine engine)
    {
        super(engine);

        registerComponentType(PositionComponent.class);
        registerComponentType(PhysicsComponent.class);
        registerComponentType(RespawnComponent.class);


        registerEventType(CheckpointEvent.class);
    }

    @Override
    protected void updateEntity(Entity entity, float dt)
    {
        PositionComponent p = posMapper.get(entity);
        PhysicsComponent phys = physMapper.get(entity);

        p.position.set(currentRespawnCoordinate);
        p.previousPosition.set(currentRespawnCoordinate);
        phys.body.setTransform(currentRespawnCoordinate, 0);

        for(JointEdge edge : phys.body.getJointList())
        {
            edge.other.setTransform(currentRespawnCoordinate, 0);
        }

        entity.removeComponent(RespawnComponent.class);
    }

    @Override
    protected void handleEvent(Event event)
    {
        if(event instanceof CheckpointEvent)
        {
            PositionComponent p = posMapper.get(event.entity);
            currentRespawnCoordinate.set(p.position);
        }
    }
}
