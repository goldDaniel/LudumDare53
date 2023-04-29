package com.ecs.events;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.ecs.Entity;

public class CollisionEndEvent extends Event
{
    public final Fixture thisFixture;
    public final Fixture otherFixture;

    public CollisionEndEvent(Entity entity, Fixture thisFixture, Fixture other)
    {
        super(entity);
        this.thisFixture = thisFixture;
        this.otherFixture = other;
    }
}
