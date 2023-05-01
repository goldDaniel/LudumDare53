package com.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.core.ContactListenerGroup;
import com.core.GameConstants;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.*;
import com.ecs.events.CameraUpdateEvent;
import com.ecs.events.Event;
import com.ecs.events.ResizeEvent;

public class PhysicsSystem extends System
{
    private static World world;
    private static ContactListenerGroup listeners;

    private static final Box2DDebugRenderer  debugRenderer = new Box2DDebugRenderer();
    Viewport viewport = new ExtendViewport(GameConstants.CAMERA_DIMENSIONS,GameConstants.CAMERA_DIMENSIONS);

    public static PhysicsComponent createComponentFromDefinition(Entity entity, BodyDef def, FixtureDef fixDef)
    {
        PhysicsComponent result = new PhysicsComponent();

        result.body = world.createBody(def);
        result.body.setUserData(entity);
        result.fixture = result.body.createFixture(fixDef);

        return result;
    }

    public static void addContactListener(ContactListener listener)
    {
        listeners.addListener(listener);
    }

    public static Joint createJoint(RevoluteJointDef joint)
    {
        return world.createJoint(joint);
    }


    private ComponentMapper<PositionComponent> posMapper = ComponentMapper.getFor(PositionComponent.class);
    private ComponentMapper<PhysicsComponent> physicsMapper = ComponentMapper.getFor(PhysicsComponent.class);
    private ComponentMapper<DrawComponent> drawMapper = ComponentMapper.getFor(DrawComponent.class);

    public PhysicsSystem(Engine engine)
    {
        super(engine);

        if(world != null)
        {
            world.dispose();
        }
        world = new World(new Vector2(0.f, -80.f), true);
        listeners = new ContactListenerGroup();
        world.setContactListener(listeners);

        registerComponentType(PositionComponent.class);
        registerComponentType(PhysicsComponent.class);

        registerEventType(ResizeEvent.class);
        registerEventType(CameraUpdateEvent.class);
    }

    @Override
    protected void preUpdate()
    {
        super.preUpdate();
        world.step(engine.getPhysicsUpdateRate(), 12, 12);
    }

    private void HandlePlayerRenderRotation(Entity player)
    {
        PhysicsComponent phys = physicsMapper.get(player);
        DrawComponent d = drawMapper.get(player);

        if(!player.hasComponent(InAirComponent.class))
        {
            float velocityX = phys.body.getLinearVelocity().x;
            if (Math.abs(velocityX) > 0)
            {
                if (velocityX < 0)
                {
                    d.rotation = 180;
                }
                else
                {
                    d.rotation = 0;
                }
            }
        }
        else
        {
            d.rotation = MathUtils.radiansToDegrees * phys.body.getAngle();
        }

        while (d.rotation < 0)
        {
            d.rotation += 360;
        }
        while(d.rotation > 360)
        {
            d.rotation -= 360;
        }

        if(d.rotation > 90 && d.rotation < 270)
        {
            d.flipY = true;
        }
        else
        {
            d.flipY = false;
        }
        phys.body.setTransform(phys.body.getPosition(), MathUtils.degreesToRadians * d.rotation);
    }


    protected void updateEntity(Entity e, float dt)
    {
        PositionComponent p = posMapper.get(e);
        PhysicsComponent phys = physicsMapper.get(e);

        p.previousPosition.set(p.position);
        p.position.set(phys.body.getPosition());

        if(e.hasComponent(DrawComponent.class))
        {
            DrawComponent d = drawMapper.get(e);
            d.rotation = MathUtils.radiansToDegrees * phys.body.getAngle();
        }

        // only player has input component. Could also check tag?
        if(e.hasComponent(InputComponent.class))
        {
            HandlePlayerRenderRotation(e);
        }
    }

    @Override
    protected void postUpdate()
    {
        super.postUpdate();
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
