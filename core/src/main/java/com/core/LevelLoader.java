package com.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.ecs.Engine;
import com.ecs.Entity;
import com.ecs.components.*;
import com.ecs.events.*;
import com.ecs.systems.PhysicsSystem;
import com.ecs.systems.RenderSystem;

public class LevelLoader
{
    private static final int tileSize = 32;
    private static final String filepath = "levels\\";


    public static void loadFromFile(Engine ecsEngine, String filename, RenderSystem render)
    {
        String levelName = filepath + filename;

        JsonReader reader = new JsonReader();
        JsonValue root =  reader.parse(Gdx.files.internal( levelName));

        for(JsonValue level : root.get("levels"))
        {

            float worldXOffset =  level.getInt("worldX") / tileSize * GameConstants.WORLD_SCALE;
            float worldYOffset =  level.getInt("worldY") / tileSize * GameConstants.WORLD_SCALE;

            // add level zones for camera bounds
            {
                Entity cameraZone = ecsEngine.createEntity();

            }

            // add entities and tiles
            JsonValue collisionLayer = null;
            JsonValue entityLayer = null;
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

            if(entityLayer != null)
            {
                loadLevelEntities(ecsEngine, entityLayer, worldXOffset, worldYOffset);
            }
            if(collisionLayer != null)
            {
                loadLevelTiles(ecsEngine, collisionLayer, worldXOffset, worldYOffset, render);
            }
        }
    }

    private static void loadLevelTiles(Engine ecsEngine, JsonValue collisionLayer, float worldXOffset, float worldYOffset, RenderSystem render)
    {
        String tilesetPath = filepath + collisionLayer.getString("__tilesetRelPath");
        Texture tileset = RenderResources.getTexture(tilesetPath);

        int[][] tileValues = new int[collisionLayer.getInt("__cWid")][collisionLayer.getInt("__cHei")];
        for(JsonValue tile :  collisionLayer.get("autoLayerTiles"))
        {
            int xIndex = tile.get("px").getInt(0) / tileSize;
            int yIndex = tile.get("px").getInt(1) / tileSize;

            float x = (xIndex * GameConstants.WORLD_SCALE + worldXOffset) ;
            float y = (-yIndex * GameConstants.WORLD_SCALE - worldYOffset);
            tileValues[xIndex][yIndex] = 1;

            int textureRegionX = tile.get("src").getInt(0);
            int textureRegionY = tile.get("src").getInt(1);


            TextureRegion region = new TextureRegion(tileset);
            region.setRegionX(textureRegionX);
            region.setRegionY(textureRegionY);
            region.setRegionWidth(tileSize);
            region.setRegionHeight(tileSize);

            render.submitTile(x, y, GameConstants.WORLD_SCALE, GameConstants.WORLD_SCALE, region);
        }

        int worldWidthTiles = tileValues.length;
        int worldHeightTiles = tileValues[0].length;

        for(int y = 0; y < worldHeightTiles; y++)
        {
            float layerWidth = GameConstants.WORLD_SCALE;
            for (int x = 0; x < worldWidthTiles; x++)
            {
                if (tileValues[x][y] == 1 && x < worldWidthTiles - 1 && tileValues[x + 1][y] == 1)
                {
                    layerWidth += 1 * GameConstants.WORLD_SCALE;
                }
                else if (tileValues[x][y] == 1 &&
                    ((x < worldWidthTiles - 1 && tileValues[x + 1][y] == 0) || x == worldWidthTiles - 1))
                {
                    float posX = x * GameConstants.WORLD_SCALE;
                    float posY = y * GameConstants.WORLD_SCALE;

                    float height = GameConstants.WORLD_SCALE;

                    Entity e = ecsEngine.createEntity();
                    PositionComponent p = e.addComponent(new PositionComponent());
                    p.position.set(posX - layerWidth / 2.f + GameConstants.WORLD_SCALE / 2 + worldXOffset, -posY - worldYOffset);
                    p.previousPosition.set(p.position);

                    BodyDef bodyDef = new BodyDef();
                    bodyDef.type = BodyDef.BodyType.StaticBody;
                    bodyDef.position.set(p.position);

                    FixtureDef fixDef = new FixtureDef();

                    PolygonShape shape = new PolygonShape();
                    shape.setAsBox(layerWidth / 2.f, height / 2.f);

                    fixDef.shape = shape;
                    fixDef.friction = 0.9f;

                    e.addComponent(PhysicsSystem.createComponentFromDefinition(e, bodyDef, fixDef));
                    layerWidth = GameConstants.WORLD_SCALE;
                }
            }
        }
    }

    private static void loadLevelEntities(Engine ecsEngine, JsonValue entityLayer, float worldXOffset, float worldYOffset)
    {
        for(JsonValue entity : entityLayer.get("entityInstances"))
        {
            float worldX = entity.get("__grid").getFloat(0) * GameConstants.WORLD_SCALE + worldXOffset;
            float worldY = -entity.get("__grid").getFloat(1) * GameConstants.WORLD_SCALE - worldYOffset;

            if(entity.getString("__identifier").equals("Player"))
            {
                createPlayerEntity(ecsEngine, worldX, worldY);
            }
            else if(entity.getString("__identifier").equals("Crate"))
            {
                float width = entity.getFloat("width")  / tileSize * GameConstants.WORLD_SCALE;
                float height = entity.getFloat("height") / tileSize * GameConstants.WORLD_SCALE;

                createCrateEntity(ecsEngine, worldX, worldY, width, height);
            }
            else if(entity.getString("__identifier").equals("DeathZone"))
            {
                float width = entity.getFloat("width")  / tileSize * GameConstants.WORLD_SCALE;
                float height = entity.getFloat("height") / tileSize * GameConstants.WORLD_SCALE;

                createDeathZoneEntity(ecsEngine, worldX, worldY, width, height);
            }
            else if(entity.getString("__identifier").equals("WinZone"))
            {
                float width = entity.getFloat("width")  / tileSize * GameConstants.WORLD_SCALE;
                float height = entity.getFloat("height") / tileSize * GameConstants.WORLD_SCALE;

                createWinZoneEntity(ecsEngine, worldX, worldY, width, height);
            }
            else if(entity.getString("__identifier").equals("Checkpoint"))
            {
                float width = entity.getFloat("width")  / tileSize * GameConstants.WORLD_SCALE;
                float height = entity.getFloat("height") / tileSize * GameConstants.WORLD_SCALE;

                createCheckpointZoneEntity(ecsEngine, worldX, worldY, width, height);
            }
        }
    }

    private static void createCheckpointZoneEntity(Engine ecsEngine, float worldX, float worldY, float width, float height)
    {
        Entity checkpoint = ecsEngine.createEntity();
        PositionComponent posComp = checkpoint.addComponent(new PositionComponent());
        posComp.position.set(worldX, worldY);
        posComp.previousPosition.set(worldX, worldY);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(worldX, worldY);

        FixtureDef fixDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2.0f, height / 2.0f);
        fixDef.shape = shape;
        fixDef.isSensor = true;

        checkpoint.addComponent(PhysicsSystem.createComponentFromDefinition(checkpoint, bodyDef, fixDef));

        ContactListener listener = new ContactListener()
        {
            @Override
            public void beginContact(Contact contact)
            {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();
                if(fixA.getBody().getUserData() != checkpoint)
                {
                    Fixture temp = fixA;
                    fixA = fixB;
                    fixB = temp;
                }

                if(fixA.getBody().getUserData() == checkpoint)
                {
                    Entity player = (Entity)fixB.getBody().getUserData();
                    if(player.hasComponent(TagComponent.class))
                    {
                        if(player.getComponent(TagComponent.class).tag.equals("player"))
                        {
                            ecsEngine.fireEvent(new CheckpointEvent(checkpoint));
                        }
                    }
                }
            }

            @Override
            public void endContact(Contact contact)  {}

            @Override
            public void preSolve(Contact contact, Manifold oldManifold){}

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse)  {}
        };

        PhysicsSystem.addContactListener(listener);
    }

    private static void createWinZoneEntity(Engine ecsEngine, float worldX, float worldY, float width, float height)
    {
        Entity winZone = ecsEngine.createEntity();
        PositionComponent posComp = winZone.addComponent(new PositionComponent());
        posComp.position.set(worldX, worldY);
        posComp.previousPosition.set(worldX, worldY);

        winZone.addComponent(new DrawComponent()).scale.set(width, height);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(worldX, worldY);

        FixtureDef fixDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2.0f, height / 2.0f);
        fixDef.shape = shape;
        fixDef.isSensor = true;

        winZone.addComponent(PhysicsSystem.createComponentFromDefinition(winZone, bodyDef, fixDef));


        ContactListener listener = new ContactListener()
        {
            @Override
            public void beginContact(Contact contact)
            {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();
                if(fixA.getBody().getUserData() != winZone)
                {
                    Fixture temp = fixA;
                    fixA = fixB;
                    fixB = temp;
                }

                if(fixA.getBody().getUserData() == winZone)
                {
                    Entity player = (Entity)fixB.getBody().getUserData();
                    if(player.hasComponent(TagComponent.class))
                    {
                        if(player.getComponent(TagComponent.class).tag.equals("player"))
                        {
                            Entity e = ecsEngine.createEntity();
                            ecsEngine.fireEvent(new PauseEvent(null));
                            e.addComponent(new GameOverComponent());
                        }
                    }
                }
            }

            @Override
            public void endContact(Contact contact)  {}

            @Override
            public void preSolve(Contact contact, Manifold oldManifold){}

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse)  {}
        };

        PhysicsSystem.addContactListener(listener);
    }

    private static void createDeathZoneEntity(Engine ecsEngine, float worldX, float worldY, float width, float height)
    {
        Entity deathZone = ecsEngine.createEntity();
        PositionComponent posComp = deathZone.addComponent(new PositionComponent());
        posComp.position.set(worldX, worldY);
        posComp.previousPosition.set(worldX, worldY);

        deathZone.addComponent(new DrawComponent()).scale.set(width, height);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(worldX, worldY);

        FixtureDef fixDef = new FixtureDef();
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2.0f, height / 2.0f);
        fixDef.shape = shape;
        fixDef.isSensor = true;

        deathZone.addComponent(PhysicsSystem.createComponentFromDefinition(deathZone, bodyDef, fixDef));


        ContactListener listener = new ContactListener()
        {
            @Override
            public void beginContact(Contact contact)
            {
                Fixture fixA = contact.getFixtureA();
                Fixture fixB = contact.getFixtureB();
                if(fixA.getBody().getUserData() != deathZone)
                {
                    Fixture temp = fixA;
                    fixA = fixB;
                    fixB = temp;
                }

                if(fixA.getBody().getUserData() == deathZone)
                {
                    Entity player = (Entity)fixB.getBody().getUserData();
                    if(player.hasComponent(TagComponent.class))
                    {
                        if(player.getComponent(TagComponent.class).tag.equals("player"))
                        {
                            player.addComponent(new RespawnComponent());
                        }
                    }
                }
            }

            @Override
            public void endContact(Contact contact)  {}

            @Override
            public void preSolve(Contact contact, Manifold oldManifold){}

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse)  {}
        };

        PhysicsSystem.addContactListener(listener);
    }

    private static void createCrateEntity(Engine ecsEngine, float worldX, float worldY, float width, float height)
    {
        Entity crate = ecsEngine.createEntity();

        PositionComponent posComp = crate.addComponent(new PositionComponent());
        posComp.position.set(worldX, worldY);
        posComp.previousPosition.set(worldX, worldY);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(worldX, worldY);
        bodyDef.gravityScale = 0.15f;

        FixtureDef fixDef = new FixtureDef();

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2.0f, height / 2.0f);

        fixDef.shape = shape;
        fixDef.density = 0.0001f;
        fixDef.friction = 1.0f;

        crate.addComponent(PhysicsSystem.createComponentFromDefinition(crate, bodyDef, fixDef));

        DrawComponent d = crate.addComponent(new DrawComponent());
        d.texture.setRegion(RenderResources.getTexture("textures/entities/crate.png"));
        d.scale.set(width, height);
    }

    private static void createPlayerEntity(Engine ecsEngine, float worldX, float worldY)
    {
        float width = 2.5f * GameConstants.WORLD_SCALE;
        float height = 0.25f * GameConstants.WORLD_SCALE;

        Entity e = ecsEngine.createEntity();
        PositionComponent p = e.addComponent(new PositionComponent());
        p.position.set(worldX, worldY);
        p.previousPosition.set(p.position);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(p.position);
        bodyDef.gravityScale = 0.5f;

        FixtureDef fixDef = new FixtureDef();

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2.f, height / 2.f);

        fixDef.shape = shape;
        fixDef.density = 0.01f / GameConstants.WORLD_SCALE;

        e.addComponent(PhysicsSystem.createComponentFromDefinition(e, bodyDef, fixDef));
        e.addComponent(new InAirComponent());

        e.addComponent(new InputComponent());
        BombComponent b = e.addComponent(new BombComponent());
        b.maxBombs = 1;
        b.bombsAvailable = b.maxBombs;

        DrawComponent d = e.addComponent(new DrawComponent());
        d.scale.set(2.77f * 2 * GameConstants.WORLD_SCALE, 1 * 2 * GameConstants.WORLD_SCALE);
        d.texture.setRegion(RenderResources.getTexture("textures/entities/car.png"));
        createWheel(ecsEngine, e, width / 2 - width / 6.f, 0f);
        createWheel(ecsEngine, e, width / 6.f - width / 2.f, 0f);

        TagComponent c = e.addComponent(new TagComponent());
        c.tag = "player";
    }

    private static void createWheel(Engine ecsEngine, Entity e, float offsetX, float offsetY)
    {
        PositionComponent entityPos = e.getComponent(PositionComponent.class);

        BodyDef frontWheel = new BodyDef();
        frontWheel.type = BodyDef.BodyType.DynamicBody;
        frontWheel.angularDamping = 500f;
        frontWheel.position.set(entityPos.position).add(offsetX, offsetY);

        FixtureDef wheelDef = new FixtureDef();

        CircleShape cs = new CircleShape();
        cs.setPosition(new Vector2());
        cs.setRadius(0.75f * GameConstants.WORLD_SCALE);
        wheelDef.shape = cs;
        wheelDef.density = 0.001f / GameConstants.WORLD_SCALE;
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
}
