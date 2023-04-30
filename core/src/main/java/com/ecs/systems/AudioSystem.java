package com.ecs.systems;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Queue;
import com.core.AudioResources;
import com.ecs.Engine;
import com.ecs.System;
import com.ecs.events.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        registerEventType(PauseEvent.class);
    }

    @Override
    protected void handleEvent(Event event)
    {
        if(event instanceof StartEvent)
        {
            if(looping.size() > 0)
            {
                for(Map.Entry<String, Audio> entry : looping.entrySet())
                {
                    Audio a = entry.getValue();
                    a.sound.loop(a.volume);
                }
            }
            else
            {
                sounds.addLast(new Audio("audio\\sfx\\idle.wav", 0.1f * AudioResources.getMasterVolume(), true));
            }
        }
        else if(event instanceof BombEvent)
        {
            sounds.addLast(new Audio("audio\\sfx\\explosion.ogg", 0.2f * AudioResources.getMasterVolume(), false));
        }
        else if(event instanceof LandEvent)
        {
            sounds.addLast(new Audio("audio\\sfx\\land.wav", 0.1f * AudioResources.getMasterVolume(), false));
        }
        else if(event instanceof PauseEvent)
        {
            for(Map.Entry<String, Audio> entry : looping.entrySet())
            {
                entry.getValue().sound.stop();
            }
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
