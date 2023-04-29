package com.ecs.events;

import com.ecs.Entity;

public class WheelOnGroundEvent extends Event
{
    public final boolean onGround;

    public WheelOnGroundEvent(Entity entity, boolean onGround)
    {
        super(entity);
        this.onGround = onGround;
    }
}
