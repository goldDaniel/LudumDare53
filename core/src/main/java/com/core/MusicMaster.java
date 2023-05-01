package com.core;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Queue;

public class MusicMaster
{
    private static boolean hasInitialized = false;

    private static float volumeFactor = 1;
    private static Music currentSong;
    private static Queue<Music> queuedSongs = new Queue<>();

    public static void init()
    {
        if(hasInitialized) throw new IllegalStateException("Music Master already initialized");
        hasInitialized = true;
    }

    public static void setMusic(Music song, float volume)
    {
        queuedSongs.clear();
        stopMusic();
        currentSong = song;
        setVolume(volume);
        currentSong.setPosition(0);
        currentSong.play();
    }

    public static void playMusic(String songName, boolean isLooping, float volume)
    {
        queuedSongs.clear();
        Music song = AudioResources.getMusic("audio\\music\\" + songName + ".wav");
        song.setLooping(isLooping);
        if(currentSong != null && currentSong.isPlaying() && !currentSong.equals(song))
        {
            currentSong.stop();
            song.setPosition(0);
        }

        currentSong = song;
        setVolume(volume);
        currentSong.play();
    }

    public static void playSequentialMusic(boolean finalIsLooping, float volume, String... songNames)
    {
        queuedSongs.clear();
        for(int i = 1; i < songNames.length; i++)
        {
            Music queuedSong = AudioResources.getMusic("audio\\music\\" + songNames[i] + ".wav");
            queuedSong.setVolume(volume);
            if(i == songNames.length - 1)
            {
                queuedSong.setLooping(finalIsLooping);
            }
            else
            {
                queuedSong.setLooping(false);
            }

            queuedSongs.addLast(queuedSong);
        }

        stopMusic();
        currentSong = AudioResources.getMusic("audio\\music\\" + songNames[0] + ".wav");
        setVolume(volume);
        currentSong.setPosition(0);
        currentSong.play();
    }

    public static void step()
    {
        if(currentSong != null && !currentSong.isPlaying())
        {
            if(queuedSongs.size > 0)
            {
                currentSong = queuedSongs.removeFirst();
                currentSong.play();
                setVolumeRelative(volumeFactor);
            }
        }
    }

    public static void stopMusic()
    {
        if(currentSong!= null && currentSong.isPlaying()) currentSong.stop();
    }

    public static void setVolume(float volume)
    {
        if(currentSong!= null) currentSong.setVolume(volume);
    }

    public static void setVolumeRelative(float factor)
    {
        volumeFactor = Math.min(Math.max(factor, 0.f), 1.f);
        if(currentSong!= null) currentSong.setVolume(currentSong.getVolume() * factor);
    }
}
