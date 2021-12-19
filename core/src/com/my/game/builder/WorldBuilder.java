package com.my.game.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.game.MyInstance;
import com.my.game.script.AircraftScript;
import com.my.game.script.ExitScript;
import com.my.game.script.GUIScript;
import com.my.game.script.GunScript;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.Camera;
import com.my.utils.world.com.Render;
import com.my.utils.world.sys.*;

public class WorldBuilder {

    public static Model skyModel;

    public static World createWorld() {
        World world = new World();

        // Init System
        world.getSystemManager().addSystem(new CameraSystem());
        world.getSystemManager().addSystem(new RenderSystem());
        world.getSystemManager().addSystem(new PhysicsSystem());
        world.getSystemManager().addSystem(new ScriptSystem());
        world.getSystemManager().addSystem(new EnvironmentSystem());
        world.getSystemManager().addSystem(new KeyInputSystem());
        world.getSystemManager().addSystem(new ConstraintSystem());
        world.start();

        // Init Assets
        initAssets(world);
        Environment environment = world.getSystemManager().getSystem(EnvironmentSystem.class).getCommonEnvironment();
        environment.set(world.getAssetsManager().getAsset("commonEnvironment", Environment.class));

        // ----- Init Static Objects ----- //
        MyInstance sky = new MyInstance(world.getAssetsManager(), "sky");
        sky.setId("sky");
        sky.getComponent(Render.class).includeEnv = false;
        world.getEntityManager().addEntity(sky);
        world.getSystemManager().getSystem(CameraSystem.class).addSkyBox("sky");
        MyInstance ground = new MyInstance(world.getAssetsManager(), "ground");
        ground.setId("ground");
        world.getEntityManager().addEntity(ground);

        // ----- Init Dynamic Objects ----- //
        AircraftBuilder aircraftBuilder = new AircraftBuilder(world);
        GunBuilder gunBuilder = new GunBuilder(world);
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
        aircraftEntity.getComponent(AircraftScript.class).body.addComponent(new Camera(0, 0, 1, 1, 0, CameraSystem.FollowType.A));

        Entity gunEntity = gunBuilder.createGun("ground", new Matrix4().translate(0, 0, -20));
        gunEntity.getComponent(GunScript.class).disabled = true;
        gunEntity.getComponent(GunScript.class).barrel.addComponent(new Camera(0, 0.7f, 0.3f, 1, 1, CameraSystem.FollowType.A));

        Entity exitScriptEntity = new Entity();
        exitScriptEntity.setId("exitScriptEntity");
        exitScriptEntity.addComponent(new ExitScript());
        world.getEntityManager().addEntity(exitScriptEntity);

        // Init GUI
        Entity guiEntity = new Entity();
        guiEntity.setId("guiEntity");
        guiEntity.addComponent(new GUIScript());
        world.getEntityManager().addEntity(guiEntity);

        return world;
    }

    public static void initAssets(World world) {

        AssetsManager assetsManager = world.getAssetsManager();
        AircraftBuilder.initAssets(assetsManager);
        GunBuilder.initAssets(assetsManager);
        ObjectBuilder.initAssets(assetsManager);

        // ----- Init Models ----- //
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();
        ArrayMap<String, Model> models = new ArrayMap<>();
        models.put("sky", skyModel);
        models.put("ground", mdBuilder.createBox(10000f, 0.01f, 20000f, new Material(ColorAttribute.createDiffuse(Color.WHITE)), attributes));

        // ----- Init Configs ----- //
        assetsManager.addAsset("sky", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("sky")));
        assetsManager.addAsset("ground", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("ground")));

        assetsManager.addAsset("ground", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btBoxShape(new Vector3(5000,0.005f,10000)), 0f));

        Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.2f, -0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.2f, 0.8f, -1f));
        world.getAssetsManager().addAsset("commonEnvironment", Environment.class, environment);
    }
}
