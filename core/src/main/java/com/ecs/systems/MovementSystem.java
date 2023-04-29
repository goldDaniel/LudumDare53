package com.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
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
        Vector2 angularSpeed = new Vector2();

        if(Gdx.input.isKeyJustPressed(i.up))
        {
            p.body.applyLinearImpulse(new Vector2(0.f, 0.25f), p.body.getPosition(), true);
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
        if(Gdx.input.isKeyPressed(Input.Keys.Q))
        {
            angularSpeed.y += 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.E))
        {
            angularSpeed.y -= 1;
        }

        if(angularSpeed.len2() > 0)
        {
            angularSpeed.nor().scl(0.2f);
            p.body.applyTorque(angularSpeed.y, true);
        }

        if(speed.len2() > 0)
        {
            speed.nor().scl(.25f);
            p.body.applyForceToCenter(speed, true);
        }
    }
}
