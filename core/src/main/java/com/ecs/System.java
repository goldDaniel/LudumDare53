package com.ecs;

import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
import com.ecs.events.Event;

public abstract class System extends com.badlogic.ashley.core.EntitySystem
{
    private final Array<Class<? extends Component>> components = new Array<>();
    private final Array<Class<? extends Event>> events = new Array<>();

    protected final Engine engine;
    protected com.badlogic.ashley.core.Engine ashleyEngine;

    protected ImmutableArray<com.badlogic.ashley.core.Entity> entities;

    public System(Engine engine)
    {
        super();
        this.engine = engine;
    }

    @Override
    public void addedToEngine(com.badlogic.ashley.core.Engine engine)
    {
        ashleyEngine = engine;

        Class<? extends com.badlogic.ashley.core.Component>[] clazz = new Class[components.size];
        for(int i = 0; i < components.size; i++)
        {
            clazz[i] = components.get(i);
        }

        entities = ashleyEngine.getEntitiesFor(Family.all(clazz).get());
    }

    public void registerComponentType(Class<? extends Component> clazz)
    {
        if(components.contains(clazz, true)) throw new IllegalStateException("System already has component registered");

        components.add(clazz);
    }

    public void registerEventType(Class<? extends Event> clazz)
    {
        if(events.contains(clazz, true)) throw new IllegalStateException("System already has event registered");

        events.add(clazz);
    }

    public final void update(float dt)
    {
        preUpdate();

        for(com.badlogic.ashley.core.Entity e : entities)
        {
            updateEntity((Entity)e, dt);
        }

        postUpdate();
    }

    public final void receiveEvent(Event event)
    {
        if(events.contains(event.getClass(), true))
        {
            handleEvent(event);
        }
    }

    protected void handleEvent(Event event) {}

    protected void preUpdate() {}

    protected void updateEntity(Entity entity, float dt) { }

    protected void postUpdate() {}
}
