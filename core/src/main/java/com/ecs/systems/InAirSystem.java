package com.ecs.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.core.Collisions;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.System;
import com.ecs.components.InAirComponent;
import com.ecs.components.InputComponent;
import com.ecs.events.CollisionEndEvent;
import com.ecs.events.CollisionStartEvent;
import com.ecs.events.Event;
import com.ecs.events.LandEvent;

public class InAirSystem extends System
{
    private ArrayMap<Entity, Array<Fixture>> touchingTop = new ArrayMap<>();
    private ArrayMap<Entity, Boolean> eventFired = new ArrayMap<>();

    public InAirSystem(Engine engine)
    {
        super(engine);

        registerComponentType(InputComponent.class);

        registerEventType(CollisionStartEvent.class);
        registerEventType(CollisionEndEvent.class);
    }

    @Override
    protected void updateEntity(Entity entity, float dt)
    {
        if(touchingTop.containsKey(entity))
        {
            if(touchingTop.get(entity).size >= 2)
            {
                entity.removeComponent(InAirComponent.class);
                if(!eventFired.containsKey(entity))
                {
                    engine.fireEvent(new LandEvent(entity));
                    eventFired.put(entity, Boolean.TRUE);
                }
            }
            else
            {
                if(!entity.hasComponent(InAirComponent.class))
                {
                    entity.addComponent(new InAirComponent());
                    if(eventFired.containsKey(entity))
                    {
                        eventFired.removeKey(entity);
                    }
                }
            }
        }

    }

    @Override
    protected void handleEvent(Event event)
    {
        if(event instanceof CollisionStartEvent)
        {
            CollisionStartEvent e = (CollisionStartEvent)event;

            if(!touchingTop.containsKey(e.entity)) touchingTop.put(e.entity, new Array<>());

            if(isTouchingTop(e.thisFixture, e.otherFixture)) touchingTop.get(e.entity).add(e.otherFixture);
        }
        else if(event instanceof CollisionEndEvent)
        {
            CollisionEndEvent e = (CollisionEndEvent)event;
            if(touchingTop.containsKey(e.entity))
            {
                touchingTop.get(e.entity).removeValue(e.otherFixture, true);
            }
        }
    }

    // determines if fixA is touching top of fixB
    public static boolean isTouchingTop(Fixture fixA, Fixture fixB)
    {
        Rectangle r0 = new Rectangle();
        r0.width = fixA.getShape().getRadius() * 2;
        r0.height = fixA.getShape().getRadius() * 2;
        r0.x = fixA.getBody().getPosition().x - r0.width / 2;
        r0.y = fixA.getBody().getPosition().y - r0.height / 2;

        Rectangle r1 = new Rectangle();
        PolygonShape terrainShape = (PolygonShape)fixB.getShape();
        Vector2 vert = new Vector2();
        Vector2 min = new Vector2(Integer.MAX_VALUE, Integer.MAX_VALUE);
        Vector2 max = new Vector2(Integer.MIN_VALUE, Integer.MIN_VALUE);
        for(int i = 0; i < terrainShape.getVertexCount(); i++)
        {
            terrainShape.getVertex(i, vert);
            if(vert.x > max.x) max.x = vert.x;
            else if(vert.x < min.x) min.x = vert.x;

            if(vert.y > max.y) max.y = vert.y;
            else if(vert.y < min.y) min.y = vert.y;
        }
        r1.width = max.x - min.x;
        r1.height = max.y - min.y;
        r1.x = fixB.getBody().getPosition().x - r1.width / 2;
        r1.y = fixB.getBody().getPosition().y - r1.height / 2;

        Collisions.CollisionSide side = Collisions.getCollisionSide(r0, r1);

        return side == Collisions.CollisionSide.Top;
    }
}
