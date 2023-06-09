package com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.core.RenderResources;

public class SettingsScreen extends GameScreen
{
    private Image settingsImage;
    public SettingsScreen(Game game)
    {
        super(game);
        settingsImage = new Image(new Texture("textures\\menu\\menu.png"));
        settingsImage.setWidth(viewport.getWorldWidth());
        settingsImage.setHeight(viewport.getWorldHeight());
    }

    @Override
    public void buildUI(Table table, Skin skin)
    {
        table.top();

        Label settingsLabel = new Label("Settings", skin, "splash_title");
        table.add(settingsLabel).padTop(100).row();

        CheckBox fullscreenCheckbox = new CheckBox("  Fullscreen", skin, "checkbox_settings");
        fullscreenCheckbox.setChecked(Gdx.graphics.isFullscreen());
        fullscreenCheckbox.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                boolean isFullscreen = Gdx.graphics.isFullscreen();
                Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
                if(isFullscreen)
                {
                    Gdx.graphics.setWindowedMode(1280, 720);
                }
                else
                {
                    Gdx.graphics.setFullscreenMode(mode);
                }
            }
        });
        table.add(fullscreenCheckbox).padTop(20).row();

        TextButton backButton = new TextButton("Back", skin, "text_button_main_menu");
        backButton.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                transitionTo(new MainMenuScreen(game));
            }
        });
        table.add(backButton).padTop(100);
    }

    @Override
    public void update(float dt)
    {

    }

    @Override
    public void render()
    {
        ScreenUtils.clear(0,0,0,1);
        RenderResources.getSpriteBatch().begin();
        settingsImage.draw(RenderResources.getSpriteBatch(), 1);
        RenderResources.getSpriteBatch().end();
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        settingsImage.setWidth(viewport.getWorldWidth());
        settingsImage.setHeight(viewport.getWorldHeight());
    }
}
