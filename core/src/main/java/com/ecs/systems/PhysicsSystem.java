package com.ecs.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.DrawComponent;
import com.ecs.components.PhysicsComponent;
import com.ecs.components.PositionComponent;

public class PhysicsSystem extends System
{
    private static World world;

    public static PhysicsComponent createComponentFromDefinition(BodyDef def, FixtureDef fixDef)
    {
        PhysicsComponent result = new PhysicsComponent();

        result.body = world.createBody(def);
        result.fixture = result.body.createFixture(fixDef);

        return result;
    }

    public static void createJoint(Body bodyA, Body bodyB)
    {
        WeldJointDef defJoint = new WeldJointDef();
        defJoint.initialize(bodyA, bodyB, new Vector2(0, 0));
        world.createJoint(defJoint);
    }

    public PhysicsSystem(Engine engine)
    {
        super(engine);

        if(world != null)
        {
            world.dispose();
        }
        world = new World(new Vector2(0.f, -50.f), true);


        registerComponentType(PositionComponent.class);
        registerComponentType(PhysicsComponent.class);
    }

    @Override
    protected void preUpdate()
    {
        super.preUpdate();
        world.step(engine.getPhysicsUpdateRate(), 10, 5);
    }

    protected void updateEntity(Entity e, float dt)
    {
        PositionComponent p = e.getComponent(PositionComponent.class);
        PhysicsComponent phys = e.getComponent(PhysicsComponent.class);

        p.previousPosition.set(p.position);
        p.position.set(phys.body.getPosition());

        if(e.hasComponent(DrawComponent.class))
        {
            DrawComponent d = e.getComponent(DrawComponent.class);
            d.rotation = MathUtils.radiansToDegrees * phys.body.getAngle();
        }
    }
}
