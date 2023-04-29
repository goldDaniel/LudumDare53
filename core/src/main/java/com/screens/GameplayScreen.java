package com.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.core.RenderResources;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.components.*;
import com.ecs.events.CameraUpdateEvent;
import com.ecs.events.ResizeEvent;
import com.ecs.systems.*;

import javax.swing.text.html.HTML;


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

        ecsEngine.registerGameSystem(new MovementSystem(ecsEngine));
        ecsEngine.registerPhysicsSystem(new PhysicsSystem(ecsEngine));

        ecsEngine.registerRenderSystem(new CameraUpdateSystem(ecsEngine));
        //ecsEngine.registerRenderSystem(new RenderSystem(ecsEngine, RenderResources.getSpriteBatch()));

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

            for(int y = 0; y < worldHeightTiles; y++)
            {
                int layerWidth = 1;
                for(int x = 0; x < worldWidthTiles; x++)
                {
                    if(tileValues[x][y] == 1)
                    {
                        float width = 1;
                        float height = 1;

                        Entity e = ecsEngine.createEntity();
                        PositionComponent p = (PositionComponent)e.addComponent(new PositionComponent());
                        p.position.set(x + 0.5f, 0.5f - y);
                        p.previousPosition.set(p.position);

                        DrawComponent d = (DrawComponent)e.addComponent(new DrawComponent());
                        d.scale.set(width, height);
                    }

                    if(tileValues[x][y] == 1 && x < worldWidthTiles - 1 && tileValues[x + 1][y] == 1)
                    {
                        layerWidth++;
                    }
                    else if(tileValues[x][y] == 1 &&
                          ((x < worldWidthTiles - 1 && tileValues[x + 1][y] == 0) || x == worldWidthTiles - 1))
                    {
                        float width = (float)layerWidth;
                        float height = 1;

                        Entity e = ecsEngine.createEntity();
                        PositionComponent p = (PositionComponent)e.addComponent(new PositionComponent());
                        p.position.set(x - layerWidth / 2.f + 0.5f, 0.5f - y);
                        p.previousPosition.set(p.position);

                        BodyDef bodyDef = new BodyDef();
                        bodyDef.type = BodyDef.BodyType.StaticBody;
                        bodyDef.position.set(p.position);

                        FixtureDef fixDef = new FixtureDef();

                        PolygonShape shape = new PolygonShape();
                        shape.setAsBox(width / 2.f,height / 2.f);

                        fixDef.shape = shape;
                        fixDef.friction = 0.9f;

                        e.addComponent(PhysicsSystem.createComponentFromDefinition(e, bodyDef, fixDef));
                        layerWidth = 1;
                    }
                }
            }
        }

        {
            JsonValue player = root.get("entities").get("Player").get(0);

            float worldX = player.getFloat("x") / tileSize + tileSize / 2.f;
            float worldY = -player.getFloat("y") / tileSize + tileSize / 2.f;

            float width = 2.5f;
            float height = 0.25f;

            Entity e = ecsEngine.createEntity();
            PositionComponent p = (PositionComponent)e.addComponent(new PositionComponent());
            p.position.set(worldX, worldY);
            p.previousPosition.set(p.position);

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(p.position);

            FixtureDef fixDef = new FixtureDef();

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(width / 2.f, height / 2.f);

            fixDef.shape = shape;
            fixDef.density = 0.1f;

            e.addComponent(PhysicsSystem.createComponentFromDefinition(e, bodyDef, fixDef));

            e.addComponent(new InputComponent());

            DrawComponent d = (DrawComponent)e.addComponent(new DrawComponent());
            d.scale.set(width, height);
            d.currentColor.set(Color.RED);
            d.texture = RenderResources.getTexture("textures/entities/car.png");
            createWheel(e, width / 2 - width / 6.f, -0.7f);
            createWheel(e, width / 6.f - width / 2.f, -0.7f);

            TagComponent c = (TagComponent) e.addComponent(new TagComponent());
            c.tag = "player";
        }
    }

    private void createWheel(Entity e, float offsetX, float offsetY)
    {
        BodyDef frontWheel = new BodyDef();
        frontWheel.type = BodyDef.BodyType.DynamicBody;
        frontWheel.angularDamping = 500f;

        FixtureDef wheelDef = new FixtureDef();

        CircleShape cs = new CircleShape();
        cs.setPosition(new Vector2());
        cs.setRadius(0.45f);
        wheelDef.shape = cs;
        wheelDef.density = 0.01f;
        wheelDef.friction = 0.8f;
        wheelDef.restitution = 0.3f;

        Entity wheel = ecsEngine.createEntity();

        PhysicsComponent wheelP = PhysicsSystem.createComponentFromDefinition(wheel, frontWheel, wheelDef);
        RevoluteJointDef frontWheelJoint = new RevoluteJointDef();
        frontWheelJoint.bodyA = e.getComponent(PhysicsComponent.class).body;
        frontWheelJoint.bodyB = wheelP.body;
        frontWheelJoint.collideConnected = false;
        frontWheelJoint.localAnchorA.set(offsetX,offsetY);
        PhysicsSystem.createJoint(frontWheelJoint);

        wheel.addComponent(wheelP);
        PositionComponent wheelPos = (PositionComponent)wheel.addComponent(new PositionComponent());
        wheelPos.position.set(wheelP.body.getPosition());
        wheelPos.previousPosition.set(wheelPos.position);

        DrawComponent wheelDraw = (DrawComponent)wheel.addComponent(new DrawComponent());
        wheelDraw.scale.set(0.5f, 0.5f);
        wheelDraw.currentColor.set(Color.GREEN);

        ContactListener listener = new ContactListener()
        {
            @Override
            public void beginContact(Contact contact)
            {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();
                if(fixA.getBody().getUserData() == wheel)
                {
                    if(fixB.getBody().getType() == BodyDef.BodyType.StaticBody)
                    {
                        Array<JointEdge> joints = fixA.getBody().getJointList();
                        Entity e = (Entity)joints.get(0).other.getUserData();
                        e.removeComponent(InAirComponent.class);
                    }
                }
                else if(fixB.getBody().getUserData() == wheel)
                {
                    if(fixA.getBody().getType() == BodyDef.BodyType.StaticBody)
                    {
                        Array<JointEdge> joints = fixB.getBody().getJointList();
                        Entity e = (Entity)joints.get(0).other.getUserData();
                        e.removeComponent(InAirComponent.class);
                    }
                }
            }

            @Override
            public void endContact(Contact contact)
            {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();
                if(fixA.getBody().getUserData() == wheel)
                {
                    if(fixB.getBody().getType() == BodyDef.BodyType.StaticBody)
                    {
                        Array<JointEdge> joints = fixA.getBody().getJointList();
                        Entity e = (Entity)joints.get(0).other.getUserData();
                        if(!e.hasComponent(InAirComponent.class)) e.addComponent(new InAirComponent());
                    }
                }
                else if(fixB.getBody().getUserData() == wheel)
                {
                    if(fixA.getBody().getType() == BodyDef.BodyType.StaticBody)
                    {
                        Array<JointEdge> joints = fixB.getBody().getJointList();
                        Entity e = (Entity)joints.get(0).other.getUserData();
                        if(!e.hasComponent(InAirComponent.class)) e.addComponent(new InAirComponent());
                    }
                }
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold)
            {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse)
            {

            }
        };

        PhysicsSystem.addContactListener(listener);
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
        ecsEngine.render(renderAlpha);
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        ecsEngine.fireEvent(new ResizeEvent(null, width, height));
    }
}
