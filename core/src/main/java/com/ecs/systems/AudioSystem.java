package com.ecs.systems;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Queue;
import com.core.AudioResources;
import com.ecs.Engine;
import com.ecs.System;
import com.ecs.events.BombEvent;
import com.ecs.events.Event;
import com.ecs.events.LandEvent;
import com.ecs.events.StartEvent;

import java.util.HashMap;
import java.util.Map;

public class AudioSystem extends System
{
    private static class Audio
    {
        private final String filepath;
        private final Sound sound;
        private final float volume;
        private final boolean loops;

        private Audio(String filepath, float volume, boolean loops)
        {
            this.filepath = filepath;
            this.sound = AudioResources.getSoundEffect(filepath);
            this.volume = volume;
            this.loops = loops;
        }
    }

    private final Queue<Audio> sounds = new Queue<>();
    private final Map<String, Audio> looping = new HashMap<>();

    public AudioSystem(Engine engine)
    {
        super(engine);

        registerEventType(StartEvent.class);
        registerEventType(BombEvent.class);
        registerEventType(LandEvent.class);
    }

    @Override
    protected void handleEvent(Event event)
    {
        if(event instanceof StartEvent)
        {
            sounds.addLast(new Audio("audio\\sfx\\idle.wav", 0.1f * AudioResources.getMasterVolume(), true));
        }
        else if(event instanceof BombEvent)
        {
            sounds.addLast(new Audio("audio\\sfx\\explosion.ogg", 0.2f * AudioResources.getMasterVolume(), false));
        }
        else if (event instanceof LandEvent)
        {
            sounds.addLast(new Audio("audio\\sfx\\land.wav", 0.1f * AudioResources.getMasterVolume(), false));
        }
    }

    @Override
    protected void postUpdate()
    {
        while(sounds.notEmpty())
        {
            Audio a = sounds.removeFirst();
            if(a.loops && !looping.containsKey(a.filepath))
            {
                looping.put(a.filepath, a);
                a.sound.loop(a.volume);
            }
            else
            {
                a.sound.play(a.volume);
            }
        }
    }
}
