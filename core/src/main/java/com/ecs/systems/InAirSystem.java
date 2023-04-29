package com.ecs.systems;

import com.badlogic.gdx.utils.ArrayMap;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.InAirComponent;
import com.ecs.components.InputComponent;
import com.ecs.events.Event;
import com.ecs.events.WheelOnGroundEvent;

public class InAirSystem extends System
{
    private ArrayMap<Entity, Integer> onGroundCount = new ArrayMap<>();

    public InAirSystem(Engine engine)
    {
        super(engine);

        registerComponentType(InputComponent.class);

        registerEventType(WheelOnGroundEvent.class);
    }

    @Override
    protected void updateEntity(Entity entity, float dt)
    {
        if(onGroundCount.containsKey(entity))
        {
            if(onGroundCount.get(entity) == 2)
            {
                entity.removeComponent(InAirComponent.class);
            }
            else
            {
                if(!entity.hasComponent(InAirComponent.class))
                {
                    entity.addComponent(new InAirComponent());
                }
            }
        }
    }

    @Override
    protected void handleEvent(Event event)
    {
        if(event instanceof  WheelOnGroundEvent)
        {
            WheelOnGroundEvent e = (WheelOnGroundEvent)event;

            int current = 0;
            if(onGroundCount.containsKey(e.entity))
            {
                current = onGroundCount.get(e.entity);
            }
            current += e.onGround ? 1 : -1;
            if(current < 0) current = 0;

            onGroundCount.put(e.entity, current);
        }
    }
}
