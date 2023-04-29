package com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.core.RenderResources;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.components.DrawComponent;
import com.ecs.components.InputComponent;
import com.ecs.components.PositionComponent;
import com.ecs.events.CameraUpdateEvent;
import com.ecs.events.ResizeEvent;
import com.ecs.systems.MovementSystem;
import com.ecs.systems.RenderSystem;


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

        ecsEngine.registerPhysicsSystem(new MovementSystem(ecsEngine));
        ecsEngine.registerRenderSystem(new RenderSystem(ecsEngine, RenderResources.getSpriteBatch()));

        Entity e = ecsEngine.createEntity();

        e.addComponent(new PositionComponent());
        e.addComponent(new InputComponent());

        DrawComponent d = (DrawComponent)e.addComponent(new DrawComponent());
        d.scale.x = 10;
        d.scale.y = 10;

        ecsEngine.fireEvent(new CameraUpdateEvent(null, new OrthographicCamera(128, 128)));
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


        ecsEngine.gameUpdate(dt);


        accumulator += dt;

        while(accumulator >= timeStep)
        {
            ecsEngine.physicsUpdate(timeStep);
            accumulator -= timeStep;
        }

        elapsedTime += dt;

        renderAlpha = accumulator / timeStep;
    }

    @Override
    public void render()
    {
        ScreenUtils.clear(Color.BLACK);
        ecsEngine.render(renderAlpha);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        ecsEngine.fireEvent(new ResizeEvent(null, width, height));
    }
}
