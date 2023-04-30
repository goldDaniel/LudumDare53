package com.ecs.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Queue;
import com.core.GameConstants;
import com.core.InputState;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.InAirComponent;
import com.ecs.components.InputComponent;
import com.ecs.components.PhysicsComponent;
import com.ecs.events.Event;
import com.ecs.events.MovementEvent;

public class MovementSystem extends System
{
    private ArrayMap<Entity, InputState> movementState = new ArrayMap<>();

    public MovementSystem(Engine engine)
    {
        super(engine);

        registerComponentType(InputComponent.class);
        registerComponentType(PhysicsComponent.class);

        registerEventType(MovementEvent.class);
    }

    @Override
    public void updateEntity(Entity entity, float dt)
    {
        PhysicsComponent p = entity.getComponent(PhysicsComponent.class);

        if(movementState.containsKey(entity))
        {
            InputState i = movementState.get(entity);
            movementState.removeKey(entity);

            Vector2 speed = new Vector2();
            float angularSpeed = 0;

            if(i.down)
            {
                speed.y -= 1;
            }

            if(entity.hasComponent(InAirComponent.class))
            {
                if(i.left)
                {
                    angularSpeed += 1;
                }
                if(i.right)
                {
                    angularSpeed -= 1;
                }
            }
            else
            {
                if(i.left)
                {
                    speed.x -= 1;
                }
                if(i.right)
                {
                    speed.x += 1;
                }
            }

            if(Math.abs(angularSpeed) > 0)
            {
                p.body.applyTorque(angularSpeed * 0.05f * GameConstants.WORLD_SCALE, true);
                float angularVel = p.body.getAngularVelocity();
                angularVel = MathUtils.clamp(angularVel, -0.5f, 0.5f);
                p.body.setAngularVelocity(angularVel);
            }
            else
            {
                float angularVel = p.body.getAngularVelocity();
                angularVel *= 0.5;
                p.body.setAngularVelocity(angularVel);
            }

            if(speed.len2() > 0)
            {
                float maxXVelocity = 30;

                Vector2 velocity = p.body.getLinearVelocity();
                // if we are going in a different direction than last frame
                if(speed.x * velocity.x < 0)
                {
                    p.body.setLinearVelocity(0, velocity.y);
                }

                speed.nor().scl(0.2f);

                if(velocity.x < maxXVelocity)
                {
                    p.body.applyForceToCenter(speed, true);
                }
            }
            else  if(!entity.hasComponent(InAirComponent.class))
            {
                Vector2 velocity = p.body.getLinearVelocity();
                velocity.x *= 0.5;
                p.body.setLinearVelocity(velocity);

                for(JointEdge j : p.body.getJointList())
                {
                    float v = j.other.getAngularVelocity();
                    v*= 0.5;
                    j.other.setAngularVelocity(v);
                }
            }
        }
    }

    @Override
    protected void handleEvent(Event event)
    {
        if(event instanceof MovementEvent)
        {
            InputState state = ((MovementEvent) event).input;

            if(movementState.containsKey(event.entity))
            {
                // only update if true
                InputState current = movementState.get(event.entity);
                if(state.left) current.left = true;
                if(state.right) current.right = true;
                if(state.up) current.up = true;
                if(state.down) current.down = true;

                if(state.action) current.action = true;
            }
            else
            {
                movementState.put(event.entity, state);
            }
        }
    }
}
