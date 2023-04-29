package com.ecs.events;

import com.badlogic.gdx.graphics.Camera;
import com.ecs.Entity;

public class CameraUpdateEvent extends Event
{
    public final Camera cam;

    public CameraUpdateEvent(Entity entity, Camera cam)
    {
        super(entity);
        this.cam = cam;
    }
}
