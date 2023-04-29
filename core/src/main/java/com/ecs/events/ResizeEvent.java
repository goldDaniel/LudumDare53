package com.ecs.events;

import com.ecs.Entity;

public class ResizeEvent extends Event
{
    public final int width;
    public final int height;

    public ResizeEvent(Entity entity, int w, int h)
    {
        super(entity);
        width = w;
        height = h;
    }
}
