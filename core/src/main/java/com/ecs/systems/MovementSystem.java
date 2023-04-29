package com.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.InAirComponent;
import com.ecs.components.InputComponent;
import com.ecs.components.PhysicsComponent;
import com.ecs.components.PositionComponent;

public class MovementSystem extends System
{
    public MovementSystem(Engine engine)
    {
        super(engine);

        registerComponentType(InputComponent.class);
        registerComponentType(PhysicsComponent.class);
    }

    @Override
    public void updateEntity(Entity entity, float dt)
    {
        InputComponent i = entity.getComponent(InputComponent.class);
        PhysicsComponent p = entity.getComponent(PhysicsComponent.class);

        Vector2 speed = new Vector2();
        float angularSpeed = 0;

        if(Gdx.input.isKeyJustPressed(i.up) && !entity.hasComponent(InAirComponent.class))
        {
            p.body.applyLinearImpulse(new Vector2(0.f, 1.0f), p.body.getPosition(), true);
        }
        if(Gdx.input.isKeyPressed(i.down))
        {
            speed.y -= 1;
        }
        if(Gdx.input.isKeyPressed(i.left))
        {
            speed.x -= 1;
        }
        if(Gdx.input.isKeyPressed(i.right))
        {
            speed.x += 1;
        }
        if(Gdx.input.isKeyPressed(i.cwRotate))
        {
            angularSpeed += 1;
        }
        if(Gdx.input.isKeyPressed(i.ccwRotate))
        {
            angularSpeed -= 1;
        }
        if(Gdx.input.isKeyJustPressed(i.action))
        {
            p.body.applyLinearImpulse(new Vector2(500.f, 0.f), p.body.getPosition(), true);
        }

        if(angularSpeed != 0)
        {
            angularSpeed.nor().scl(0.4f);
            p.body.applyTorque(angularSpeed.y, true);
        }
        else
        {
            float velocity = p.body.getAngularVelocity();
            velocity *= 0.5;
            p.body.setAngularVelocity(velocity);
        }

        if(speed.len2() > 0)
        {
            float maxXVelocity = 30;


            Vector2 velocity = p.body.getLinearVelocity();
            // if are are going in a different direction than last frame
            if(speed.x * velocity.x < 0)
            {
                p.body.setLinearVelocity(0, velocity.y);
            }

            speed.nor().scl(.50f);
            p.body.applyForceToCenter(speed, true);

            velocity = p.body.getLinearVelocity();
            velocity.x = MathUtils.clamp(velocity.x, -maxXVelocity, maxXVelocity);
            p.body.setLinearVelocity(velocity);
        }
        else
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
        else if(!entity.hasComponent(InAirComponent.class))
        {
            Vector2 vel = p.body.getLinearVelocity();
            vel.x *= 0.95f;
            p.body.setLinearVelocity(vel);
        }

        //float clampX = MathUtils.clamp(p.body.getLinearVelocity().x, -15, 15);
        //p.body.setLinearVelocity(clampX, p.body.getLinearVelocity().y);
    }
}
