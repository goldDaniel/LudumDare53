package com.ecs.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ArrayMap;
import com.core.InputState;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.BombComponent;
import com.ecs.components.InAirComponent;
import com.ecs.components.InputComponent;
import com.ecs.components.PhysicsComponent;
import com.ecs.events.BombEvent;
import com.ecs.events.Event;
import com.ecs.events.LandEvent;
import com.ecs.events.MovementEvent;

public class BombSystem extends System
{
    private ArrayMap<Entity, InputState> actionState = new ArrayMap<>();

    public BombSystem(Engine engine)
    {
        super(engine);

        registerComponentType(InputComponent.class);
        registerComponentType(PhysicsComponent.class);
        registerComponentType(BombComponent.class);

        registerEventType(BombEvent.class);
        registerEventType(LandEvent.class);
    }

    @Override
    public void updateEntity(Entity entity, float dt)
    {
        PhysicsComponent p = entity.getComponent(PhysicsComponent.class);
        BombComponent b = entity.getComponent(BombComponent.class);

        if(actionState.containsKey(entity))
        {
            InputState i = actionState.get(entity);
            actionState.removeKey(entity);

            boolean goUp = i.up && b.bombAvailable;
            boolean goForward = i.action && b.bombAvailable;

            if((goUp || goForward) && entity.hasComponent(InAirComponent.class))
            {
                b.bombAvailable = false;
            }

            if(goUp)
            {
                Vector2 up = new Vector2(1, 0);
                float angle = p.body.getAngle();
                while(angle > 2 * Math.PI)
                {
                    angle -= 2 * Math.PI;
                }
                while(angle < 0)
                {
                    angle += 2 * Math.PI;
                }
                up.rotateRad(angle);

                if(angle >= 3.f / 2.f * Math.PI || angle < Math.PI / 2.f)
                {
                    up.rotateRad((float)Math.PI / 2.f);
                }
                else
                {
                    up.rotateRad(-(float)Math.PI / 2.f);
                }

                p.body.applyLinearImpulse(up.scl(0.75f), p.body.getPosition(), true);
            }

            if(goForward)
            {
                Vector2 forward = new Vector2(1, 0);
                float angle = p.body.getAngle();
                while(angle > 2 * Math.PI)
                {
                    angle -= 2 * Math.PI;
                }
                while(angle < 0)
                {
                    angle += 2 * Math.PI;
                }
                forward.rotateRad(angle);
                p.body.applyLinearImpulse(forward.scl(100), p.body.getPosition(), true);
            }
        }
    }

    @Override
    protected void handleEvent(Event event)
    {
        if(event instanceof BombEvent)
        {
            InputState state = ((BombEvent) event).input;

            if(actionState.containsKey(event.entity))
            {
                // only update if true
                InputState current = actionState.get(event.entity);
                if(state.up) current.up = true;
                if(state.action) current.action = true;
            }
            else
            {
                actionState.put(event.entity, state);
            }
        }
        if(event instanceof LandEvent)
        {
            Entity e = ((LandEvent)event).entity;

            if(e.hasComponent(BombComponent.class))
            {
               e.getComponent(BombComponent.class).bombAvailable = true;
            }
        }
    }
}
