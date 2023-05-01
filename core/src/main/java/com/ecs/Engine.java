package com.ecs;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.ecs.events.Event;

public class Engine
{
    private final Array<Entity> activeEntities = new Array<>();
    private final Array<Entity> createdEntities = new Array<>();
    private final Array<Entity> deadEntities = new Array<>();

    private com.badlogic.ashley.core.Engine renderSystems;
    private com.badlogic.ashley.core.Engine physicsSystems;
    private com.badlogic.ashley.core.Engine gameSystems;

    private boolean isUpdating = false;

    private float physicsUpdateRate = 1.0f/60.f;

    public Engine()
    {
        renderSystems = new com.badlogic.ashley.core.Engine();
        physicsSystems = new com.badlogic.ashley.core.Engine();
        gameSystems = new com.badlogic.ashley.core.Engine();
    }

    public <T extends System> void registerPhysicsSystem(T system)
    {
        physicsSystems.addSystem(system);
    }

    public <T extends System> void registerGameSystem(T system)
    {
        gameSystems.addSystem(system);
    }

    public <T extends System> void registerRenderSystem(T system)
    {
        renderSystems.addSystem(system);
    }


    public Entity createEntity()
    {
        Entity e = Entity.create();

        if(isUpdating)
        {
            createdEntities.add(e);
        }
        else
        {
            activeEntities.add(e);
            gameSystems.addEntity(e);
            physicsSystems.addEntity(e);
            renderSystems.addEntity(e);
        }

        return e;
    }

    public void destroyEntity(Entity entity)
    {
        if(!activeEntities.contains(entity, true)) throw new IllegalArgumentException("Entity is not in ECS!!");

        if(isUpdating)
        {
            deadEntities.add(entity);
        }
        else
        {
            activeEntities.removeValue(entity, true);
            Entity.destroy(entity);
        }
    }

    public Array<Entity> getEntities(Class<? extends Component>... components)
    {
        return getEntities(new Array<>(components));
    }

    public Array<Entity> getEntities(Array<Class> components)
    {
        Array<Entity> result = new Array<>();
        ImmutableArray<com.badlogic.ashley.core.Entity> entities = gameSystems.getEntitiesFor(Family.all(components.toArray()).get());

        for(com.badlogic.ashley.core.Entity e : entities)
        {
            result.add((Entity)e);
        }
        return result;
    }

    public void setPhysicsUpdateRate(float delta)
    {
        physicsUpdateRate = delta;
    }

    public float getPhysicsUpdateRate()
    {
        return physicsUpdateRate;
    }

    public void physicsUpdate()
    {
        isUpdating = true;
        {
            physicsSystems.update(physicsUpdateRate);
        }
        isUpdating = false;
    }

    public void gameUpdate(float dt)
    {
        isUpdating = true;
        {
            gameSystems.update(dt);
        }

        isUpdating = false;

        activeEntities.removeAll(deadEntities, true);

        for(Entity e : deadEntities)
        {
            gameSystems.removeEntity(e);
            physicsSystems.removeEntity(e);
            renderSystems.removeEntity(e);
        }
        Entity.destroy(deadEntities);
        deadEntities.clear();

        activeEntities.addAll(createdEntities);
        for(Entity e : createdEntities)
        {
            gameSystems.addEntity(e);
            physicsSystems.addEntity(e);
            renderSystems.addEntity(e);
        }

        createdEntities.clear();
    }

    public void render(float alpha)
    {
        renderSystems.update(alpha);
    }

    public void fireEvent(Event event)
    {
        for (EntitySystem sys : physicsSystems.getSystems())
        {
            System s = (System)sys;
            s.receiveEvent(event);
        }
        for (EntitySystem sys : gameSystems.getSystems())
        {
            System s = (System)sys;
            s.receiveEvent(event);
        }
        for (EntitySystem sys : renderSystems.getSystems())
        {
            System s = (System)sys;
            s.receiveEvent(event);
        }
    }
}
