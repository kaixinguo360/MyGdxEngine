package com.my.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.World;
import com.my.utils.world.sys.*;

public class WorldBuilder {

    public static Model skyModel;

    public static MyGame.GameWorld createWorld() {
        World world = new World();

        // Init System
        world.getSystemManager().addSystem(new RenderSystem());
        world.getSystemManager().addSystem(new PhysicsSystem());
        world.getSystemManager().addSystem(new SerializationSystem());
        world.getSystemManager().addSystem(new ConstraintSystem());
        world.getSystemManager().addSystem(new ScriptSystem());

        // Init Assets
        initAssets(world);

        // ----- Init Static Objects ----- //
        MyInstance sky = new MyInstance(world.getAssetsManager(), "sky");
        sky.setId("sky");
        world.getEntityManager().addEntity(sky);
        MyInstance ground = new MyInstance(world.getAssetsManager(), "ground");
        ground.setId("ground");
        world.getEntityManager().addEntity(ground);

        // ----- Init Dynamic Objects ----- //
        Aircrafts.AircraftBuilder aircraftBuilder = new Aircrafts.AircraftBuilder(world);
        Guns.GunBuilder gunBuilder = new Guns.GunBuilder(world);
        ObjectBuilder objectBuilder = new ObjectBuilder(world);
        for (int i = 0; i < 100; i++) {
            objectBuilder.createBox(new Matrix4().translate(10, 0.5f, -10 * i), ground.getId());
            objectBuilder.createBox(new Matrix4().translate(-10, 0.5f, -10 * i), ground.getId());
        }
        for (int i = 1; i < 5; i++) {
            objectBuilder.createTower(new Matrix4().setToTranslation(-5, 0, -200 * i), 5 * i);
        }
        for (int x = -20; x <= 20; x+=40) {
            for (int y = 0; y <= 0; y+=20) {
                for (int z = -20; z <= 20; z+=20) {
                    aircraftBuilder.createAircraft(new Matrix4().translate(x, y, z), 4000, 40);
                }
            }
        }

        Entity aircraftEntity = aircraftBuilder.createAircraft(new Matrix4().translate(0, 0, 200), 8000, 40);
        aircraftEntity.addComponent(new Aircrafts.AircraftScript());

        Entity gunEntity = gunBuilder.createGun("ground", new Matrix4().translate(0, 0, -20));
        gunEntity.addComponent(new Guns.GunScript()).disabled = true;

        // Init World Entity Filters
        world.update();

        // Create LoaderManager
        LoaderManager loaderManager = new MyGame.GameLoaderManager();

        return new MyGame.GameWorld(world, loaderManager);
    }

    public static void initAssets(World world) {

        AssetsManager assetsManager = world.getAssetsManager();
        Aircrafts.initAssets(assetsManager);
        Guns.initAssets(assetsManager);
        ObjectBuilder.initAssets(assetsManager);

        // ----- Init Models ----- //
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();
        ArrayMap<String, Model> models = new ArrayMap<>();
        models.put("sky", skyModel);
        models.put("ground", mdBuilder.createBox(10000f, 0.01f, 20000f, new Material(ColorAttribute.createDiffuse(Color.WHITE)), attributes));

        // ----- Init Configs ----- //
        assetsManager.addAsset("sky", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("sky"), false));
        assetsManager.addAsset("ground", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("ground")));

        assetsManager.addAsset("ground", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btBoxShape(new Vector3(5000,0.005f,10000)), 0f));
    }
}
