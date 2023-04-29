package com.ecs.events;

import com.ecs.Entity;

public abstract class Event
{
    public final Entity entity;

    public Event(Entity entity)
    {
        this.entity = entity;
    }
}
