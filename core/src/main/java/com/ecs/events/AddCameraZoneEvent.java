package com.ecs.events;

import com.ecs.Entity;

public class AddCameraZoneEvent extends Event
{
    public final float x;
    public final float y;
    public final float w;
    public final float h;

    public AddCameraZoneEvent(Entity entity, float worldX, float worldY, float width, float height)
    {
        super(entity);
        this.x = worldX;
        this.y =  worldY;
        this.w = width;
        this.h = height;
    }
}
