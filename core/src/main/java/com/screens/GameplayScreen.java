package com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.core.RenderResources;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.components.*;
import com.ecs.events.CameraUpdateEvent;
import com.ecs.events.ResizeEvent;
import com.ecs.systems.CameraUpdateSystem;
import com.ecs.systems.MovementSystem;
import com.ecs.systems.PhysicsSystem;
import com.ecs.systems.RenderSystem;

import javax.swing.text.html.HTML;


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

        ecsEngine.registerRenderSystem(new CameraUpdateSystem(ecsEngine));
        ecsEngine.registerRenderSystem(new RenderSystem(ecsEngine, RenderResources.getSpriteBatch()));

        loadLevelIntoECS();
    }

    private void loadLevelIntoECS()
    {
        int tileSize = 32;

        String folder = "levels\\level_test\\simplified\\Level_0\\";
        String  collisionLayerName = "Collision.csv";
        String dataName = "data.json";

        JsonReader reader = new JsonReader();
        JsonValue root =  reader.parse(Gdx.files.internal(folder + dataName));

        {
            int worldWidthTiles = root.getInt("width") / tileSize;
            int worldHeightTiles = root.getInt("height") / tileSize;

            int[][] tileValues = new int[worldWidthTiles][worldHeightTiles];

            String csv = Gdx.files.internal(folder + collisionLayerName).readString();

            int readY = 0;
            int readX = 0;
            for(String line : csv.split("\n"))
            {
                for(String value : line.split(","))
                {
                    tileValues[readX][readY] = Integer.parseInt(value);
                    readX++;
                }

                readY++;
                readX = 0;
            }

            for(int x = 0; x < worldWidthTiles; x++)
            {
                for(int y = 0; y < worldHeightTiles; y++)
                {
                    if(tileValues[x][y] == 1)
                    {
                        float width = 1;
                        float height = 1;

                        Entity e = ecsEngine.createEntity();
                        PositionComponent p = (PositionComponent)e.addComponent(new PositionComponent());
                        p.position.set(x + 0.5f, -y + 0.5f);

                        BodyDef bodyDef = new BodyDef();
                        bodyDef.type = BodyDef.BodyType.StaticBody;
                        bodyDef.position.set(p.position);

                        FixtureDef fixDef = new FixtureDef();

                        PolygonShape shape = new PolygonShape();
                        shape.setAsBox(width / 2.f,height / 2.f);

                        fixDef.shape = shape;

                        e.addComponent(PhysicsSystem.createComponentFromDefinition(bodyDef, fixDef));

                        DrawComponent d = (DrawComponent)e.addComponent(new DrawComponent());
                        d.scale.set(width, height);
                    }
                }
            }
        }


        {
            JsonValue player = root.get("entities").get("Player").get(0);

            float worldX = player.getFloat("x") / tileSize + tileSize / 2.f;
            float worldY = -player.getFloat("y") / tileSize + tileSize / 2.f;

            float width = 1;
            float height = 1;

            Entity e = ecsEngine.createEntity();
            PositionComponent p = (PositionComponent)e.addComponent(new PositionComponent());
            p.position.set(worldX, worldY);
            p.previousPosition.set(p.position);

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(p.position);

            FixtureDef fixDef = new FixtureDef();

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(width / 2.f,height / 2.f);

            fixDef.shape = shape;
            fixDef.density = 0.01f;

            TagComponent c = (TagComponent) e.addComponent(new TagComponent());
            c.tag = "player";

            e.addComponent(PhysicsSystem.createComponentFromDefinition(bodyDef, fixDef));
            e.addComponent(new InputComponent());

            DrawComponent d = (DrawComponent)e.addComponent(new DrawComponent());
            d.scale.set(width, height);
            d.currentColor.set(Color.RED);
        }

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
