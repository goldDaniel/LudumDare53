package com.ecs.events;

import com.core.InputState;
import com.ecs.Entity;

public class MovementEvent extends Event
{
    public final InputState input;

    public MovementEvent(Entity entity, InputState input)
    {
        super(entity);
        this.input = input;
    }
}
