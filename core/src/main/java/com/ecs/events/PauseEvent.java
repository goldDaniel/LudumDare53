package com.ecs.events;

import com.ecs.Entity;

public class PauseEvent extends Event
{
    public PauseEvent(Entity entity)
    {
        super(entity);
    }
}
