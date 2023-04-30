package com.ecs.events;

import com.core.InputState;
import com.ecs.Entity;

public class BombEvent extends Event
{
    public final InputState input;

    public BombEvent(Entity entity, InputState input)
    {
        super(entity);
        this.input = input;
    }
}
