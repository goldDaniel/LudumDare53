package com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.core.MusicMaster;
import com.core.RenderResources;

public class MainMenuScreen extends GameScreen
{
    private Image menuImage;
    public MainMenuScreen(Game game)
    {
        super(game);
        menuImage = new Image(new Texture("textures\\menu\\menu.png"));
        menuImage.setWidth(viewport.getWorldWidth());
        menuImage.setHeight(viewport.getWorldHeight());
    }

    @Override
    public void buildUI(Table table, Skin skin)
    {
        table.top();

        table.add(new Label("Dough or Die", skin, "splash_title"));
        table.row();

        Action playAction = new Action()
        {
            @Override
            public boolean act(float delta)
            {
                transitionTo(new CutsceneScreen(game, CutsceneScreen.CutsceneType.INTRO));
                return true;
            }
        };
        table.add(createButton("Play", playAction, table, skin)).padTop(256);
        table.row();

        Action settingsAction = new Action()
        {
            @Override
            public boolean act(float delta)
            {
                transitionTo(new SettingsScreen(game));
                return true;
            }
        };
        table.add(createButton("Settings", settingsAction, table, skin)).padTop(64);
        table.row();

        Action extiAction = Actions.sequence(Actions.fadeOut(1f), new Action()
        {
            @Override
            public boolean act(float delta)
            {
                Gdx.app.exit();
                return true;
            }
        });
        table.add(createButton("Exit", extiAction, table, skin)).padTop(64);
    }

    @Override
    public void show()
    {
        super.show();
        MusicMaster.playMusic("menu", true, 1);
        MusicMaster.setVolumeRelative(1);
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
        menuImage.draw(RenderResources.getSpriteBatch(), 1);
        RenderResources.getSpriteBatch().end();
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        menuImage.setWidth(viewport.getWorldWidth());
        menuImage.setHeight(viewport.getWorldHeight());
    }

    private TextButton createButton(String text, Action action, Table table, Skin skin)
    {
        TextButton result = new TextButton(text, skin, "text_button_main_menu");
        result.addListener(new ChangeListener()
        {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                table.addAction(action);
                result.removeListener(this);
            }
        });

        return result;
    }
}
