package com.ecs.systems;

import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.GameOverComponent;

public class GameOverSystem extends System
{
    public interface IScreenTransition
    {
        void execute();
    }

    private final IScreenTransition transition;

    public GameOverSystem(Engine engine, IScreenTransition transition)
    {
        super(engine);

        registerComponentType(GameOverComponent.class);

        this.transition = transition;
    }

    @Override
    protected void updateEntity(Entity entity, float dt)
    {
        GameOverComponent g = entity.getComponent(GameOverComponent.class);

        g.timer -= dt;
        if(g.timer <= 0)
        {
            transition.execute();
        }
    }
}
