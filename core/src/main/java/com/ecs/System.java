package com.ecs;

import com.badlogic.gdx.utils.Array;
import com.ecs.events.Event;

public abstract class System
{
    private final Array<Class<? extends Component>> components = new Array<>();
    private final Array<Class<? extends Event>> events = new Array<>();
    private boolean enabled = true;

    protected final Engine engine;

    public System(Engine engine)
    {
        this.engine = engine;
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

        for(Entity e : engine.getEntities(components))
        {
            updateEntity(e, dt);
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

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean value)
    {
        enabled = value;
    }
}
