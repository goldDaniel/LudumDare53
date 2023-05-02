package com.ecs.events;

import com.badlogic.gdx.math.Vector2;
import com.ecs.Entity;

public class BombEvent extends Event
{
    public final Vector2 pos = new Vector2();

    public BombEvent(Entity entity, Vector2 pos)
    {
        super(entity);
        this.pos.set(pos);
    }
}
