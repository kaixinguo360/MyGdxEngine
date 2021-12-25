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
import com.my.game.LoadUtil;
import com.my.game.constraint.HingeConstraint;
import com.my.game.script.ExitScript;
import com.my.game.script.GUIScript;
import com.my.game.script.GunScript;
import com.my.utils.world.*;
import com.my.utils.world.com.Camera;
import com.my.utils.world.com.Position;
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
        PrefabBuilder.initAssets(world.getAssetsManager(), world.getSystemManager());
        Environment environment = world.getSystemManager().getSystem(EnvironmentSystem.class).getCommonEnvironment();
        environment.set(world.getAssetsManager().getAsset("commonEnvironment", Environment.class));

        // ----- Init Static Objects ----- //
        BaseBuilder baseBuilder = new BaseBuilder(world);
        Entity sky = baseBuilder.createEntity("sky");
        sky.setName("sky");
        sky.getComponent(Render.class).includeEnv = false;
        world.getEntityManager().addEntity(sky);
        world.getSystemManager().getSystem(CameraSystem.class).addSkyBox(sky.getId());
        Entity ground = baseBuilder.createEntity("ground");
        ground.setName("ground");
        world.getEntityManager().addEntity(ground);

        // ----- Init Dynamic Objects ----- //

        Prefab runway = world.getAssetsManager().getAsset("Runway", Prefab.class);
        runway.newInstance(LoadUtil.loaderManager, world);

        Prefab tower = world.getAssetsManager().getAsset("Tower", Prefab.class);
        for (int i = 1; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                Entity entity = tower.newInstance(LoadUtil.loaderManager, world);
                entity.getComponent(Position.class).getLocalTransform().setToTranslation(-5, 5 * j, -200 * i);
            }
        }

        Prefab aircraft = world.getAssetsManager().getAsset("Aircraft", Prefab.class);
        int aircraftNum = 0;
        for (int x = -20; x <= 20; x+=40) {
            for (int y = 0; y <= 0; y+=20) {
                for (int z = -20; z <= 20; z+=20) {
                    Entity entity = aircraft.newInstance(LoadUtil.loaderManager, world);
                    entity.setName("Aircraft-" + aircraftNum++);
                    entity.getComponent(Position.class).getLocalTransform().setToTranslation(x, y, z);
                }
            }
        }

        Entity aircraftEntity = aircraft.newInstance(LoadUtil.loaderManager, world);
        aircraftEntity.setName("Aircraft-6");
        aircraftEntity.getComponent(Position.class).getLocalTransform().translate(0, 0, 200);
        aircraftEntity.findChildByName("body").addComponent(new Camera(0, 0, 1, 1, 0, CameraSystem.FollowType.A));

        Prefab gun = world.getAssetsManager().getAsset("Gun", Prefab.class);
        Entity gunEntity = gun.newInstance(LoadUtil.loaderManager, world);
        gunEntity.setName("Gun-0");
        gunEntity.getComponent(Position.class).getLocalTransform().translate(0, 0.01f / 2, -20);
        gunEntity.getComponent(GunScript.class).disabled = true;
        Entity rotateY = gunEntity.findChildByName("rotate_Y");
        Matrix4 rotateYTransform = new Matrix4().translate(0, 0, -20).translate(0, 0.5f + 0.01f / 2, 0);
        Matrix4 groundTransform = ground.getComponent(Position.class).getLocalTransform().cpy();
        rotateY.addComponent(new HingeConstraint(ground,
                groundTransform.inv().mul(rotateYTransform).rotate(Vector3.X, 90),
                new Matrix4().rotate(Vector3.X, 90),
                false
        ));
        gunEntity.findChildByName("barrel").addComponent(new Camera(0, 0.7f, 0.3f, 1, 1, CameraSystem.FollowType.A));

        Entity exitScriptEntity = new Entity();
        exitScriptEntity.setName("exitScriptEntity");
        exitScriptEntity.addComponent(new ExitScript());
        world.getEntityManager().addEntity(exitScriptEntity);

        // Init GUI
        Entity guiEntity = new Entity();
        guiEntity.setName("guiEntity");
        guiEntity.addComponent(new GUIScript());
        world.getEntityManager().addEntity(guiEntity);

        return world;
    }

    public static void initAssets(World world) {

        AssetsManager assetsManager = world.getAssetsManager();
        SystemManager systemManager = world.getSystemManager();
        AircraftBuilder.initAssets(assetsManager);
        GunBuilder.initAssets(assetsManager);
        SceneBuilder.initAssets(assetsManager);

        // ----- Init Models ----- //
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();

        // ----- Init Configs ----- //
        assetsManager.addAsset("sky", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(skyModel));
        assetsManager.addAsset("ground", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(mdBuilder.createBox(10000f, 0.01f, 20000f, new Material(ColorAttribute.createDiffuse(Color.WHITE)), attributes)));

        assetsManager.addAsset("ground", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btBoxShape(new Vector3(5000,0.005f,10000)), 0f));

        Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.2f, -0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.2f, 0.8f, -1f));
        world.getAssetsManager().addAsset("commonEnvironment", Environment.class, environment);
    }
}
