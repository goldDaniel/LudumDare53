package com.ecs.events;

import com.ecs.Entity;

public class StartEvent extends Event
{
    public StartEvent(Entity entity)
    {
        super(entity);
    }
}
