package com.core;

import com.badlogic.gdx.audio.Music;

public class MusicMaster
{
    private static boolean hasInitialized = false;

    private static Music currentSong;

    public static void init()
    {
        if(hasInitialized) throw new IllegalStateException("Music Master already initialized");
        hasInitialized = true;
    }

    public static void setMusic(Music song, float volume)
    {
        pauseMusic();
        currentSong = song;
        setVolume(volume);
        currentSong.setPosition(0);
        currentSong.play();
    }

    public static void playMusic(String songName, boolean isLooping, float volume)
    {
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
        for(int i = 0; i < songNames.length - 1; i++)
        {
            Music song = AudioResources.getMusic("audio\\music\\" + songNames[i] + ".wav");
            if(i == songNames.length - 2)
            {
                Music nextSong = AudioResources.getMusic("audio\\music\\" + songNames[i + 1] + ".wav");
                song.setOnCompletionListener((s) ->
                {
                    nextSong.setLooping(finalIsLooping);
                    setMusic(nextSong, volume);
                });
            }
            else
            {
                Music nextSong = AudioResources.getMusic("audio\\music\\" + songNames[i + 1] + ".wav");
                song.setOnCompletionListener((s) ->
                {
                    nextSong.setLooping(false);
                    setMusic(nextSong, volume);
                });
            }
        }

        setMusic(AudioResources.getMusic("audio\\music\\" + songNames[0] + ".wav"), volume);
    }

    public static void pauseMusic()
    {
        if(currentSong!= null && currentSong.isPlaying()) currentSong.pause();
    }

    public static void resumeMusic()
    {
        if(currentSong!= null && !currentSong.isPlaying()) currentSong.play();
    }

    public static void setVolume(float volume)
    {
        if(currentSong!= null) currentSong.setVolume(volume);
    }

    public static void setVolumeRelative(float factor)
    {
        if(currentSong!= null) currentSong.setVolume(currentSong.getVolume() * factor);
    }
}
