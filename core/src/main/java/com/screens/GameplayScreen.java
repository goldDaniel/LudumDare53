package com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.core.AudioResources;
import com.core.Collisions;
import com.core.GameConstants;
import com.core.LevelLoader;
import com.core.RenderResources;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.components.*;
import com.ecs.events.*;
import com.ecs.systems.*;


public class GameplayScreen extends GameScreen
{
    private final Engine ecsEngine;

    private float accumulator;
    private float elapsedTime;
    private float renderAlpha;

    public GameplayScreen(Game game)
    {
        super(game);
        ecsEngine = new Engine();

        ecsEngine.registerGameSystem(new InputSystem(ecsEngine));
        ecsEngine.registerGameSystem(new AudioSystem(ecsEngine));

        ecsEngine.registerPhysicsSystem(new InAirSystem(ecsEngine));
        ecsEngine.registerPhysicsSystem(new MovementSystem(ecsEngine));
        ecsEngine.registerPhysicsSystem(new BombSystem(ecsEngine));
        ecsEngine.registerPhysicsSystem(new PhysicsSystem(ecsEngine));

        ecsEngine.registerRenderSystem(new CameraUpdateSystem(ecsEngine));
        ecsEngine.registerRenderSystem(new RenderSystem(ecsEngine, RenderResources.getSpriteBatch()));
        ecsEngine.registerRenderSystem(new GameOverSystem(ecsEngine, () -> transitionTo(new MainMenuScreen(game))));;

        loadLevelIntoECS();

        ecsEngine.fireEvent(new StartEvent(null));
    }

    public void doEvent(Event event)
    {
        ecsEngine.fireEvent(event);
    }

    private void loadLevelIntoECS()
    {
        LevelLoader.loadFromFile(ecsEngine, "level_test.ldtk");
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

    Label timer;
    @Override
    public void buildUI(Table table, Skin skin)
    {
        table.top().left();
        timer = new Label("Delivery Distance: " + Math.max((TIME_LIMIT - elapsedTime) * PIZZA_GUY_SPEED, 0.f), skin, "splash_continue");
        table.add(timer);
    }

    private static final float TIME_LIMIT = 60;
    private static final float PIZZA_GUY_SPEED = 50;
    private static final float KPH_TO_MPS = 3.6f;
    @Override
    public void update(float dt)
    {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
        {
            ecsEngine.fireEvent(new PauseEvent(null));
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
        int distance = Math.max((int)((TIME_LIMIT - elapsedTime) * PIZZA_GUY_SPEED / KPH_TO_MPS), 0);
        String unit = "";
        if(distance > 1000)
        {
            distance /= 1000;
            unit += "km";
        }
        else
        {
            unit += "m";
        }

        timer.setText(String.format("Delivery Distance: %d%s", distance, unit));

        renderAlpha = accumulator / physicsRate;

        if(elapsedTime >= TIME_LIMIT)
        {
            Entity e = ecsEngine.createEntity();
            e.addComponent(new GameOverComponent());
            ecsEngine.fireEvent(new PauseEvent(null));
        }
    }

    @Override
    public void render()
    {
        ecsEngine.render(renderAlpha);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        ecsEngine.fireEvent(new ResizeEvent(null, width, height));
    }
}
