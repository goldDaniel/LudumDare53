package com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.ecs.Engine;


public class GameplayScreen extends GameScreen
{
    private Engine ecsEngine;

    private float accumulator;
    private float elapsedTime;
    private float renderAlpha;

    public GameplayScreen(Game game)
    {
        super(game);
        ecsEngine = new Engine();
    }

    @Override
    public void show()
    {
        super.show();
    }

    @Override
    public void hide()
    {
        super.hide();
    }

    @Override
    public void buildUI(Table table, Skin skin)
    {

    }

    @Override
    public void update(float dt)
    {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
        {
            game.setScreen(new PauseScreen(game,this));
        }

        final float timeStep = 1.f/60.f;

        accumulator += dt;

        while(accumulator >= timeStep)
        {
            ecsEngine.physicsUpdate(timeStep);
            accumulator -= timeStep;
        }

        ecsEngine.gameUpdate(dt);
        elapsedTime += dt;

        renderAlpha = accumulator / timeStep;
    }

    @Override
    public void render()
    {
        ScreenUtils.clear(Color.SKY);
        ecsEngine.render(renderAlpha);
    }

    @Override
    public void resize(int width, int height)
    {

    }
}
