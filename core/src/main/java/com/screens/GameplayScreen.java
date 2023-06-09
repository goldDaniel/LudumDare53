package com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.core.LevelLoader;
import com.core.MusicMaster;
import com.core.RenderResources;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.components.GameOverComponent;
import com.ecs.events.Event;
import com.ecs.events.PauseEvent;
import com.ecs.events.ResizeEvent;
import com.ecs.events.StartEvent;
import com.ecs.systems.*;


public class GameplayScreen extends GameScreen
{
    private final Engine ecsEngine;

    private float accumulator;
    private float elapsedTime;
    private float renderAlpha;
    private CutsceneScreen.CutsceneType gameResult = CutsceneScreen.CutsceneType.WIN;

    private RenderSystem renderSystem;

    public GameplayScreen(Game game)
    {
        super(game);
        ecsEngine = new Engine();

        ecsEngine.registerGameSystem(new RespawnSystem(ecsEngine));
        ecsEngine.registerGameSystem(new InputSystem(ecsEngine));
        ecsEngine.registerGameSystem(new AudioSystem(ecsEngine));
        ecsEngine.registerGameSystem(new InAirSystem(ecsEngine));
        ecsEngine.registerGameSystem(new GameOverSystem(ecsEngine, () ->
        {
            ecsEngine.fireEvent(new PauseEvent(null));
            game.setScreen(new CutsceneScreen(game, gameResult));
        }));;

        ecsEngine.registerPhysicsSystem(new MovementSystem(ecsEngine));
        ecsEngine.registerPhysicsSystem(new BombSystem(ecsEngine));
        ecsEngine.registerPhysicsSystem(new PhysicsSystem(ecsEngine));

        ecsEngine.registerRenderSystem(new CameraUpdateSystem(ecsEngine));
        renderSystem = ecsEngine.registerRenderSystem(new RenderSystem(ecsEngine, RenderResources.getSpriteBatch()));

        loadLevelIntoECS();

        MusicMaster.playSequentialMusic(true, 0.5f, "level_start", "level_loop");

        ecsEngine.fireEvent(new StartEvent(null));
    }

    private void loadLevelIntoECS()
    {
        LevelLoader.loadFromFile(ecsEngine, "level_test.ldtk", renderSystem);
    }


    @Override
    public void show()
    {
        super.show();
        ecsEngine.fireEvent(new StartEvent(null));
    }

    @Override
    public void hide()
    {
        super.hide();
        ecsEngine.fireEvent(new PauseEvent(null));
    }

    Label timer;
    @Override
    public void buildUI(Table table, Skin skin)
    {
        table.top().left();
        timer = new Label("Bomb Delivery Dist: " + Math.max((TIME_LIMIT - elapsedTime) * PIZZA_GUY_SPEED, 0.f), skin, "bomb_text");
        table.add(timer);
    }

    private static final float TIME_LIMIT = 120;
    private static final float PIZZA_GUY_SPEED = 50;
    private static final float KPH_TO_MPS = 3.6f;
    @Override
    public void update(float dt)
    {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
        {
            game.setScreen(new PauseScreen(game,this));
        }

        ecsEngine.gameUpdate(dt);

        accumulator += dt;

        float physicsRate = ecsEngine.getPhysicsUpdateRate();
        while(accumulator >= physicsRate)
        {
            ecsEngine.physicsUpdate();
            accumulator -= physicsRate;
        }

        elapsedTime += dt;
        int distance = (int)Math.max(((TIME_LIMIT - elapsedTime) * PIZZA_GUY_SPEED / KPH_TO_MPS), 0);
        String distanceString;
        if(distance > 1000)
        {
            distance /= 100;
            distanceString = distance / 10 + "." + distance % 10 + "km";
        }
        else
        {
            distanceString = distance + "m";
        }

        timer.setText("Bomb Delivery Dist: " + distanceString);

        renderAlpha = accumulator / physicsRate;

        if(elapsedTime >= TIME_LIMIT)
        {
            gameResult = CutsceneScreen.CutsceneType.LOSE;
            Entity e = ecsEngine.createEntity();
            ecsEngine.fireEvent(new PauseEvent(null));
            e.addComponent(new GameOverComponent());
        }
    }

    @Override
    public void render()
    {
        ScreenUtils.clear(0.3f,0.3f,0.3f,1);
        ecsEngine.render(renderAlpha);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        ecsEngine.fireEvent(new ResizeEvent(null, width, height));
    }
}
