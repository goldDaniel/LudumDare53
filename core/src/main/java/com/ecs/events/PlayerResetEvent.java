package com.ecs.events;

import com.ecs.Entity;

public class PlayerResetEvent extends Event
{
    public PlayerResetEvent(Entity entity)
    {
        super(entity);
    }
}
