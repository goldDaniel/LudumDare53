package com.ecs.systems;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
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

    private ComponentMapper<GameOverComponent> gameOverMapper = ComponentMapper.getFor(GameOverComponent.class);

    public GameOverSystem(Engine engine, IScreenTransition transition)
    {
        super(engine);

        registerComponentType(GameOverComponent.class);

        this.transition = transition;
    }

    @Override
    protected void updateEntity(Entity entity, float dt)
    {
        GameOverComponent g = gameOverMapper.get(entity);

        g.timer -= dt;
        if(g.timer <= 0)
        {
            transition.execute();
        }
    }
}
