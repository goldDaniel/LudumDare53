package com.ecs.components;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.ecs.Component;

public class PhysicsComponent extends Component
{
    public Body body = null;
    public Fixture fixture = null;
}
