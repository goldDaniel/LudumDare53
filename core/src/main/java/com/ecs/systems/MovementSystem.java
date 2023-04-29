package com.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.PositionComponent;

public class MovementSystem extends System
{
    public MovementSystem(Engine engine)
    {
        super(engine);

        registerComponentType(PositionComponent.class);
    }

    @Override
    public void updateEntity(Entity entity, float dt)
    {
        PositionComponent p = entity.getComponent(PositionComponent.class);

        Vector2 speed = new Vector2();

        if(Gdx.input.isKeyPressed(Input.Keys.W))
        {
            speed.y += 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S))
        {
            speed.y -= 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A))
        {
            speed.x -= 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D))
        {
            speed.x += 1;
        }

        if(speed.len2() > 0)
        {
            speed.nor().scl(32.f * dt);
        }

        p.previousPosition.set(p.position);
        p.position.add(speed);
    }
}
