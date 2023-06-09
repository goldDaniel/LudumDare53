package com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.core.MusicMaster;
import com.core.RenderResources;
import com.ecs.events.StartEvent;

public class PauseScreen extends GameScreen
{
    private final GameScreen gameScreen;

    public PauseScreen(Game game, GameScreen gameScreen)
    {
        super(game);
        this.gameScreen = gameScreen;
    }

    @Override
    public void show()
    {
        super.show();
        MusicMaster.setVolumeRelative(0.4f);
    }

    @Override
    public void hide()
    {
        super.hide();
        MusicMaster.setVolumeRelative(1 / 0.4f);
    }

    @Override
    public void buildUI(Table table, Skin skin)
    {
        table.top();

        Label pauseLabel = new Label("Paused", skin, "splash_title");
        table.add(pauseLabel).padTop(100).row();

        TextButton resumeButton = new TextButton("Resume", skin, "text_button_main_menu");
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                game.setScreen(gameScreen);
            }
        });
        table.add(resumeButton).padTop(100).row();

        TextButton menuButton = new TextButton("Menu", skin, "text_button_main_menu");
        menuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor)
            {
                transitionTo(new MainMenuScreen(game));
            }
        });
        table.add(menuButton).padTop(100);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        gameScreen.resize(width, height);
    }

    @Override
    public void update(float dt)
    {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
        {
            game.setScreen(gameScreen);
        }
    }

    @Override
    public void render()
    {
        gameScreen.render();

        ShapeRenderer s = RenderResources.getShapeRenderer();
        s.setProjectionMatrix(new Matrix4());
        Gdx.gl.glEnable(GL20.GL_BLEND);
        s.begin(ShapeRenderer.ShapeType.Filled);
        s.setColor(0, 0, 0, 0.5f);
        s.rect(-1, -1, 2, 2);
        s.end();
    }
}
