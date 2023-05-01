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

    public static void setMusic(Music song)
    {
        pauseMusic();
        currentSong = song;
        currentSong.play();
    }

    public static void playMusic(String songName, boolean isLooping)
    {
        Music song = AudioResources.getMusic("audio\\music\\" + songName + ".wav");
        song.setLooping(isLooping);
        if(currentSong != null && currentSong.isPlaying() && !currentSong.equals(song))
        {
            currentSong.stop();
        }

        currentSong = song;
        currentSong.play();
    }

    public static void playSequentialMusic(boolean finalIsLooping, String... songNames)
    {
        Music song = AudioResources.getMusic("audio\\music\\" + songNames[0] + ".wav");
        boolean isLooping = false;
        if(songNames.length > 1)
        {
            String[] nextSongNames = new String[songNames.length - 1];
            System.arraycopy(songNames, 1, nextSongNames, 0, nextSongNames.length);

            song.setOnCompletionListener((s) ->
            {
                MusicMaster.playSequentialMusic(finalIsLooping, nextSongNames);
            });
        }
        else
        {
            isLooping = finalIsLooping;
        }

        song.setLooping(isLooping);
        setMusic(song);
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
