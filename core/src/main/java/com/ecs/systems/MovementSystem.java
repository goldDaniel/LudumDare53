package com.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
            p.body.applyLinearImpulse(new Vector2(0.f, 7.f), p.body.getPosition(), true);
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
            angularSpeed *= 2.f;
            p.body.applyTorque(angularSpeed, true);
        }
        else
        {
            float omega = p.body.getAngularVelocity();
            omega *= 0.95f;
            p.body.setAngularVelocity(omega);
        }

        if(speed.len2() > 0)
        {
            speed.nor().scl(1.25f);
            p.body.applyLinearImpulse(speed, p.body.getPosition(), true);
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
