package com.ecs;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

public class Entity extends com.badlogic.ashley.core.Entity implements Pool.Poolable
{
    private static Pool<Entity> entityPool = new Pool<Entity>()
    {
        @Override
        protected Entity newObject()
        {
            return new Entity();
        }
    };

    private static int nextID = 1;

    public final int ID = nextID++;

    protected static Entity create()
    {
        Entity result = entityPool.obtain();
        return result;
    }

    protected static void destroy(Entity entity)
    {
        entityPool.free(entity);
    }
    protected static void destroy(Array<Entity> entities)
    {
        entityPool.freeAll(entities);
    }

    private Entity() {}


    public <T extends Component> T removeComponent(Class<T> clazz)
    {
        return (T)super.remove(clazz);
    }

    public <T extends Component> T addComponent(T c)
    {
        return (T)addAndReturn(c);
    }


    public boolean hasComponent(Class<? extends Component> clazz)
    {
        return getComponent(clazz) != null;
    }

    @Override
    public void reset()
    {
        super.removeAll();
    }
}
