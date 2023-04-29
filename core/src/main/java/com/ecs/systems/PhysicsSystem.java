package com.ecs.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.DrawComponent;
import com.ecs.components.PhysicsComponent;
import com.ecs.components.PositionComponent;
import com.ecs.events.CameraUpdateEvent;
import com.ecs.events.Event;
import com.ecs.events.ResizeEvent;

public class PhysicsSystem extends System
{
    private static World world;

    private static Box2DDebugRenderer  debugRenderer = new Box2DDebugRenderer();
    Viewport viewport = new ExtendViewport(32,32);


    public static PhysicsComponent createComponentFromDefinition(BodyDef def, FixtureDef fixDef)
    {
        PhysicsComponent result = new PhysicsComponent();

        result.body = world.createBody(def);
        result.fixture = result.body.createFixture(fixDef);

        return result;
    }

    public static Joint createJoint(RevoluteJointDef joint)
    {
        return world.createJoint(joint);
    }

    public PhysicsSystem(Engine engine)
    {
        super(engine);

        if(world != null)
        {
            world.dispose();
        }
        world = new World(new Vector2(0.f, -30.f), true);


        registerComponentType(PositionComponent.class);
        registerComponentType(PhysicsComponent.class);

        registerEventType(ResizeEvent.class);
        registerEventType(CameraUpdateEvent.class);
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

    @Override
    protected void postUpdate()
    {
        super.postUpdate();
        debugRenderer.render(world, viewport.getCamera().combined);
    }

    @Override
    protected void handleEvent(Event event)
    {
        if(event instanceof ResizeEvent)
        {
            ResizeEvent e = (ResizeEvent)event;
            viewport.update(e.width, e.height);
            viewport.apply();
        }
        else if(event instanceof CameraUpdateEvent)
        {
            CameraUpdateEvent e = (CameraUpdateEvent)event;
            viewport.setCamera(e.cam);
            viewport.apply();
        }
    }
}
