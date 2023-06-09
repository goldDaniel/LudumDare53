package com.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ArrayMap;
import com.core.GameConstants;
import com.core.InputState;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.*;
import com.ecs.events.BombEvent;
import com.ecs.events.Event;
import com.ecs.events.LandEvent;
import com.ecs.events.MovementEvent;

public class BombSystem extends System
{
    private final ArrayMap<Entity, InputState> actionState = new ArrayMap<>();

    private ComponentMapper<InputComponent> inputMapper = ComponentMapper.getFor(InputComponent.class);
    private ComponentMapper<PhysicsComponent> physicsMapper = ComponentMapper.getFor(PhysicsComponent.class);
    private ComponentMapper<BombComponent> bombMapper = ComponentMapper.getFor(BombComponent.class);

    public BombSystem(Engine engine)
    {
        super(engine);

        registerComponentType(InputComponent.class);
        registerComponentType(PhysicsComponent.class);
        registerComponentType(BombComponent.class);

        registerEventType(MovementEvent.class);
        registerEventType(LandEvent.class);
    }

    @Override
    public void updateEntity(Entity entity, float dt)
    {
        PhysicsComponent p = physicsMapper.get(entity);
        BombComponent b = bombMapper.get(entity);

        if(actionState.containsKey(entity))
        {
            InputState i = actionState.get(entity);
            actionState.removeKey(entity);

            boolean goUp = i.up && b.bombsAvailable > 0;
            boolean goForward = i.action && b.bombsAvailable > 0;

            if((goUp || goForward) && entity.hasComponent(InAirComponent.class))
            {
                b.bombsAvailable--;
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

                p.body.applyLinearImpulse(up.scl(0.2f * GameConstants.WORLD_SCALE), p.body.getPosition(), true);

                DrawComponent draw = entity.getComponent(DrawComponent.class);

                Vector2 pos = p.body.getPosition().cpy();
                pos.sub(up.nor().scl(draw.scale.y / 4));

                engine.fireEvent(new BombEvent(entity, pos));
            }
            else if(goForward)
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
                p.body.applyLinearImpulse(forward.scl(0.3f * GameConstants.WORLD_SCALE), p.body.getPosition(), true);


                DrawComponent draw = entity.getComponent(DrawComponent.class);

                Vector2 pos = p.body.getPosition().cpy();
                pos.sub(forward.nor().scl(draw.scale.x / 4));

                engine.fireEvent(new BombEvent(entity, pos));
            }
        }
    }

    @Override
    protected void handleEvent(Event event)
    {
        if(event instanceof MovementEvent)
        {
            InputState state = ((MovementEvent) event).input;

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
                BombComponent b = e.getComponent(BombComponent.class);
                b.bombsAvailable = b.maxBombs;
            }
        }
    }
}
