package com.ecs.events;

import com.ecs.Entity;

public class CheckpointEvent extends Event
{
    public CheckpointEvent(Entity entity)
    {
        super(entity);
    }
}
