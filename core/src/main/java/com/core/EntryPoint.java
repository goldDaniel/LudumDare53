package com.core;

import com.badlogic.gdx.Game;
import com.screens.SplashScreen;

public class EntryPoint extends Game
{
    @Override
    public void create()
    {
        RenderResources.init();
        AudioResources.init();

        setScreen(new SplashScreen(this));
    }
}
