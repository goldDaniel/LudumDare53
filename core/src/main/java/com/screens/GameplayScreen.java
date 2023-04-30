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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.core.Collisions;
import com.core.RenderResources;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.components.*;
import com.ecs.events.CollisionEndEvent;
import com.ecs.events.ResizeEvent;
import com.ecs.events.CollisionStartEvent;
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

        ecsEngine.registerPhysicsSystem(new BombSystem(ecsEngine));
        ecsEngine.registerPhysicsSystem(new MovementSystem(ecsEngine));
        ecsEngine.registerPhysicsSystem(new PhysicsSystem(ecsEngine));
        ecsEngine.registerPhysicsSystem(new InAirSystem(ecsEngine));

        ecsEngine.registerRenderSystem(new CameraUpdateSystem(ecsEngine));
        ecsEngine.registerRenderSystem(new RenderSystem(ecsEngine, RenderResources.getSpriteBatch()));

        loadLevelIntoECS();
    }

    private void loadLevelIntoECS()
    {
        int tileSize = 32;

        String filepath = "levels\\";

        JsonReader reader = new JsonReader();
        JsonValue root =  reader.parse(Gdx.files.internal( filepath + "level_test.ldtk"));

        // load map
        {
            for(JsonValue level : root.get("levels"))
            {
                int worldXOffset =  level.getInt("worldX") / tileSize;
                int worldYOffset =  level.getInt("worldY") / tileSize;

                JsonValue entityLayer = null;
                JsonValue collisionLayer = null;
                for(JsonValue layer : level.get("layerInstances"))
                {
                    if(layer.getString("__identifier").equals("Collision"))
                    {
                        collisionLayer = layer;
                    }
                    else if(layer.getString("__identifier").equals("Entities"))
                    {
                        entityLayer = layer;
                    }
                }

                for(JsonValue entity : entityLayer.get("entityInstances"))
                {
                    if(entity.getString("__identifier").equals("Player"))
                    {
                        float worldX = entity.get("__grid").getFloat(0) + worldXOffset;
                        float worldY = -entity.get("__grid").getFloat(1) - worldYOffset;

                        float width = 2.5f;
                        float height = 0.25f;

                        Entity e = ecsEngine.createEntity();
                        PositionComponent p = e.addComponent(new PositionComponent());
                        p.position.set(worldX, worldY);
                        p.previousPosition.set(p.position);

                        BodyDef bodyDef = new BodyDef();
                        bodyDef.type = BodyDef.BodyType.DynamicBody;
                        bodyDef.position.set(p.position);

                        FixtureDef fixDef = new FixtureDef();

                        PolygonShape shape = new PolygonShape();
                        shape.setAsBox(width / 2.f, height / 2.f);

                        fixDef.shape = shape;
                        fixDef.density = 0.01f;

                        e.addComponent(PhysicsSystem.createComponentFromDefinition(e, bodyDef, fixDef));
                        e.addComponent(new InAirComponent());

                        e.addComponent(new InputComponent());
                        e.addComponent(new BombComponent());

                        DrawComponent d = e.addComponent(new DrawComponent());
                        d.scale.set(2.77f * 2, 1 * 2);
                        d.texture.setRegion(RenderResources.getTexture("textures/entities/car.png"));
                        createWheel(e, width / 2 - width / 6.f, 0f);
                        createWheel(e, width / 6.f - width / 2.f, 0f);

                        TagComponent c = e.addComponent(new TagComponent());
                        c.tag = "player";
                    }
                }


                String tilesetPath = filepath + collisionLayer.getString("__tilesetRelPath");
                Texture tileset = RenderResources.getTexture(tilesetPath);

                int[][] tileValues = new int[collisionLayer.getInt("__cWid")][collisionLayer.getInt("__cHei")];
                for(JsonValue tile :  collisionLayer.get("autoLayerTiles"))
                {
                    int xIndex = tile.get("px").getInt(0) / tileSize;
                    int yIndex = tile.get("px").getInt(1) / tileSize;

                    int x = xIndex + worldXOffset;
                    int y = -yIndex - worldYOffset;
                    tileValues[xIndex][yIndex] = 1;

                    int textureRegionX = tile.get("src").getInt(0);
                    int textureRegionY = tile.get("src").getInt(1);

                    Entity tileEntity = ecsEngine.createEntity();
                    PositionComponent p = tileEntity.addComponent(new PositionComponent());
                    p.position.set(x, y);
                    p.previousPosition.set(p.position);

                    DrawComponent d = tileEntity.addComponent(new DrawComponent());
                    d.texture.setTexture(tileset);
                    d.texture.setRegionX(textureRegionX);
                    d.texture.setRegionY(textureRegionY);
                    d.texture.setRegionWidth(tileSize);
                    d.texture.setRegionHeight(tileSize);
                }

                {
                    int worldWidthTiles = tileValues.length;
                    int worldHeightTiles = tileValues[0].length;

                    for(int y = 0; y < worldHeightTiles; y++)
                    {
                        int layerWidth = 1;
                        for(int x = 0; x < worldWidthTiles; x++)
                        {
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
                                PositionComponent p = e.addComponent(new PositionComponent());
                                p.position.set(x - layerWidth / 2.f + 0.5f + worldXOffset, -y - worldYOffset);
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
            }
        }
    }

    private void createWheel(Entity e, float offsetX, float offsetY)
    {
        PositionComponent entityPos = e.getComponent(PositionComponent.class);

        BodyDef frontWheel = new BodyDef();
        frontWheel.type = BodyDef.BodyType.DynamicBody;
        frontWheel.angularDamping = 500f;
        frontWheel.position.set(entityPos.position).add(offsetX, offsetY);

        FixtureDef wheelDef = new FixtureDef();

        CircleShape cs = new CircleShape();
        cs.setPosition(new Vector2());
        cs.setRadius(0.75f);
        wheelDef.shape = cs;
        wheelDef.density = 0.001f;
        wheelDef.friction = 0.8f;

        Entity wheel = ecsEngine.createEntity();

        PhysicsComponent wheelP = PhysicsSystem.createComponentFromDefinition(wheel, frontWheel, wheelDef);
        RevoluteJointDef frontWheelJoint = new RevoluteJointDef();
        frontWheelJoint.bodyA = e.getComponent(PhysicsComponent.class).body;
        frontWheelJoint.bodyB = wheelP.body;
        frontWheelJoint.collideConnected = false;
        frontWheelJoint.localAnchorA.set(offsetX,offsetY);
        PhysicsSystem.createJoint(frontWheelJoint);

        wheel.addComponent(wheelP);
        PositionComponent wheelPos = wheel.addComponent(new PositionComponent());
        wheelPos.position.set(wheelP.body.getPosition());
        wheelPos.previousPosition.set(wheelPos.position);

        DrawComponent wheelDraw = wheel.addComponent(new DrawComponent());
        wheelDraw.scale.set(0.5f, 0.5f);
        wheelDraw.currentColor.set(Color.GREEN);

        ContactListener listener = new ContactListener()
        {
            @Override
            public void beginContact(Contact contact)
            {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();
                if(fixA.getBody().getUserData() != wheel)
                {
                    Fixture temp = fixA;
                    fixA = fixB;
                    fixB = temp;
                }

                if(fixA.getBody().getUserData() == wheel)
                {
                    if (fixB.getBody().getType() == BodyDef.BodyType.StaticBody)
                    {
                        Array<JointEdge> joints = fixA.getBody().getJointList();
                        Entity e = (Entity) joints.get(0).other.getUserData();
                        ecsEngine.fireEvent(new CollisionStartEvent(e, fixA, fixB));
                    }
                }
            }

            @Override
            public void endContact(Contact contact)
            {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();
                if(fixA.getBody().getUserData() != wheel)
                {
                    Fixture temp = fixA;
                    fixA = fixB;
                    fixB = temp;
                }

                if(fixA.getBody().getUserData() == wheel)
                {
                    if (fixB.getBody().getType() == BodyDef.BodyType.StaticBody)
                    {
                        Array<JointEdge> joints = fixA.getBody().getJointList();
                        Entity e = (Entity) joints.get(0).other.getUserData();
                        ecsEngine.fireEvent(new CollisionEndEvent(e, fixA, fixB));
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

        accumulator += dt;

        float physicsRate = ecsEngine.getPhysicsUpdateRate();
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
