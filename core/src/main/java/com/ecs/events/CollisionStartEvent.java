package com.ecs.events;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.ecs.Entity;

public class CollisionStartEvent extends Event
{
    public final Fixture thisFixture;
    public final Fixture otherFixture;

    public CollisionStartEvent(Entity entity, Fixture thisFixture, Fixture other)
    {
        super(entity);
        this.thisFixture = thisFixture;
        this.otherFixture = other;
    }
}
