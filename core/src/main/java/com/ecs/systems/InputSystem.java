package com.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.core.InputState;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.InputComponent;
import com.ecs.events.BombEvent;
import com.ecs.events.MovementEvent;

public class InputSystem extends System
{
    public InputSystem(Engine engine)
    {
        super(engine);

        registerComponentType(InputComponent.class);
    }

    @Override
    protected void updateEntity(Entity entity, float dt)
    {
        InputComponent i = entity.getComponent(InputComponent.class);

        InputState state = new InputState();
        state.left = Gdx.input.isKeyPressed(i.left);
        state.right = Gdx.input.isKeyPressed(i.right);
        state.up = Gdx.input.isKeyJustPressed(i.up);
        state.down = Gdx.input.isKeyPressed(i.down);

        state.action = Gdx.input.isKeyJustPressed(i.action);


        engine.fireEvent(new MovementEvent(entity, state));
    }
}
