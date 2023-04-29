package com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.core.RenderResources;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.components.DrawComponent;
import com.ecs.components.InputComponent;
import com.ecs.components.PhysicsComponent;
import com.ecs.components.PositionComponent;
import com.ecs.events.CameraUpdateEvent;
import com.ecs.events.ResizeEvent;
import com.ecs.systems.MovementSystem;
import com.ecs.systems.PhysicsSystem;
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

        ecsEngine.registerGameSystem(new MovementSystem(ecsEngine));
        ecsEngine.registerPhysicsSystem(new PhysicsSystem(ecsEngine));

        ecsEngine.registerRenderSystem(new RenderSystem(ecsEngine, RenderResources.getSpriteBatch()));

        {
            float width = 2;
            float height = 2;

            Entity e = ecsEngine.createEntity();
            PositionComponent p = (PositionComponent)e.addComponent(new PositionComponent());

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(p.position);

            FixtureDef fixDef = new FixtureDef();

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(width / 2.f,height / 2.f);

            fixDef.shape = shape;
            fixDef.density = 0.01f;

            e.addComponent(PhysicsSystem.createComponentFromDefinition(bodyDef, fixDef));


            e.addComponent(new InputComponent());

            DrawComponent d = (DrawComponent)e.addComponent(new DrawComponent());
            d.scale.set(width, height);
        }

        {
            float width = 32;
            float height = 5;

            Entity e = ecsEngine.createEntity();
            PositionComponent p = (PositionComponent)e.addComponent(new PositionComponent());
            p.position.set(0, -16);

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.KinematicBody;
            bodyDef.position.set(p.position);

            FixtureDef fixDef = new FixtureDef();

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(width / 2.f,height / 2.f);

            fixDef.shape = shape;

            e.addComponent(PhysicsSystem.createComponentFromDefinition(bodyDef, fixDef));

            DrawComponent d = (DrawComponent)e.addComponent(new DrawComponent());
            d.scale.set(width, height);
        }



        ecsEngine.fireEvent(new CameraUpdateEvent(null, new OrthographicCamera(32, 32)));
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


        ecsEngine.gameUpdate(dt);

        float physicsRate = ecsEngine.getPhysicsUpdateRate();
        if(dt > physicsRate * 4) dt = physicsRate * 4;

        accumulator += dt;

        while(accumulator >= physicsRate)
        {
            ecsEngine.physicsUpdate();
            accumulator -= physicsRate;
        }

        elapsedTime += dt;

        renderAlpha = accumulator / physicsRate;
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
