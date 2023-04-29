package com.ecs;

import com.badlogic.gdx.utils.Array;
import com.ecs.events.Event;

public class Engine
{
    private final Array<Entity> activeEntities = new Array<>();
    private final Array<Entity> createdEntities = new Array<>();
    private final Array<Entity> deadEntities = new Array<>();

    private final Array<System> renderSystems = new Array<>();
    private final Array<System> physicsSystems = new Array<>();
    private final Array<System> gameSystems = new Array<>();

    private boolean isUpdating = false;

    private float physicsUpdateRate = 1.0f/30.f;

    public <T extends System> void registerPhysicsSystem(T system)
    {
        physicsSystems.add(system);
    }

    public <T extends System> void registerGameSystem(T system)
    {
        gameSystems.add(system);
    }

    public <T extends System> void registerRenderSystem(T system)
    {
        renderSystems.add(system);
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

    public Array<Entity> getEntities(Array<Class<? extends Component>> components)
    {
        Array<Entity> result = new Array<>();

        for(Entity entity : activeEntities)
        {
            if(entity.hasComponent(components))
            {
                result.add(entity);
            }
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
            for (int i = 0; i < physicsSystems.size; i++)
            {
                if(physicsSystems.get(i).isEnabled())
                {
                    physicsSystems.get(i).update(physicsUpdateRate);
                }
            }
        }
        isUpdating = false;
    }

    public void gameUpdate(float dt)
    {
        isUpdating = true;
        {
            for (int i = 0; i < gameSystems.size; i++)
            {
                if(gameSystems.get(i).isEnabled())
                {
                    gameSystems.get(i).update(dt);
                }
            }
        }

        isUpdating = false;

        activeEntities.removeAll(deadEntities, true);
        Entity.destroy(deadEntities);
        deadEntities.clear();

        activeEntities.addAll(createdEntities);
        createdEntities.clear();
    }

    public void render(float alpha)
    {
        for (int i = 0; i < renderSystems.size; i++)
        {
            if(renderSystems.get(i).isEnabled())
            {
                renderSystems.get(i).update(alpha);
            }
        }
    }

    public void fireEvent(Event event)
    {
        for (System s : physicsSystems)
        {
            s.receiveEvent(event);
        }
        for (System s : gameSystems)
        {
            s.receiveEvent(event);
        }
        for (System s : renderSystems)
        {
            s.receiveEvent(event);
        }
    }

    public <T extends System> void enableSystem(Class<T> clazz)
    {
        for(System system : gameSystems)
        {
            if(system.getClass().equals(clazz))
            {
                system.setEnabled(true);
                return;
            }
        }

        for(System system : renderSystems)
        {
            if(system.getClass().equals(clazz))
            {
                system.setEnabled(true);
                return;
            }
        }
    }

    public <T extends System> void disableSystem(Class<T> clazz)
    {
        for(System system : gameSystems)
        {
            if(system.getClass().equals(clazz))
            {
                system.setEnabled(false);
                return;
            }
        }

        for(System system : renderSystems)
        {
            if(system.getClass().equals(clazz))
            {
                system.setEnabled(true);
                return;
            }
        }
    }
}
