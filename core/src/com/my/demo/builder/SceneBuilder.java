package com.my.demo.builder;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.ExitScript;
import com.my.demo.script.GUIScript;
import com.my.demo.script.GunScript;
import com.my.demo.script.ReloadScript;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Prefab;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.render.Camera;
import com.my.world.module.render.CameraSystem;
import com.my.world.module.render.Render;

import java.util.function.Function;

public class SceneBuilder {

    public static void initScene(Scene scene) {
        Engine engine = scene.getEngine();

        // ----- Init Static Objects ----- //
        Entity sky = BaseBuilder.createEntity(scene, "sky");
        sky.setName("sky");
        sky.getComponent(Render.class).includeEnv = false;
        scene.getEntityManager().addEntity(sky);
        scene.getSystemManager().getSystem(CameraSystem.class).addSkyBox(sky.getId());
        Entity ground = BaseBuilder.createEntity(scene, "ground");
        ground.setName("ground");
        scene.getEntityManager().addEntity(ground);

        // ----- Init Dynamic Objects ----- //

        Prefab runway = engine.getAssetsManager().getAsset("Runway", Prefab.class);
        scene.instantiatePrefab(runway);

        Prefab tower = engine.getAssetsManager().getAsset("Tower", Prefab.class);
        for (int i = 1; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                Entity entity = scene.instantiatePrefab(tower);
                entity.getComponent(Position.class).getLocalTransform().setToTranslation(-5, 5 * j, -200 * i);
            }
        }

        Prefab aircraft = engine.getAssetsManager().getAsset("Aircraft", Prefab.class);
        int aircraftNum = 0;
        for (int x = -20; x <= 20; x+=40) {
            for (int y = 0; y <= 0; y+=20) {
                for (int z = -20; z <= 20; z+=20) {
                    Entity entity = scene.instantiatePrefab(aircraft);
                    entity.setName("Aircraft-" + aircraftNum++);
                    entity.getComponent(Position.class).getLocalTransform().setToTranslation(x, y, z);
                }
            }
        }

        Entity aircraftEntity = scene.instantiatePrefab(aircraft);
        aircraftEntity.setName("Aircraft-6");
        aircraftEntity.getComponent(Position.class).getLocalTransform().translate(0, 0, 200);
        aircraftEntity.findChildByName("body").addComponent(new Camera(0, 0, 1, 1, 0, CameraSystem.FollowType.A));

        Prefab gun = engine.getAssetsManager().getAsset("Gun", Prefab.class);
        Entity gunEntity = scene.instantiatePrefab(gun);
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
        scene.getEntityManager().addEntity(exitScriptEntity);

        Entity reloadScriptEntity = new Entity();
        reloadScriptEntity.setName("reloadScriptEntity");
        reloadScriptEntity.addComponent(new ReloadScript());
        scene.getEntityManager().addEntity(reloadScriptEntity);

        // Init GUI
        Entity guiEntity = new Entity();
        guiEntity.setName("guiEntity");
        guiEntity.addComponent(new GUIScript()).targetEntity = scene.getEntityManager().findEntityByName("Aircraft-6");
        scene.getEntityManager().addEntity(guiEntity);
    }

    public static void init(Engine engine) {
        Scene scene = engine.getSceneManager().newScene("prefab");

        ObjectBuilder.initAssets(engine, scene);
        BulletBuilder.initAssets(engine, scene);
        GunBuilder.initAssets(engine, scene);
        AircraftBuilder.initAssets(engine, scene);

        engine.getSceneManager().removeScene(scene.getId());
    }

    public static void createPrefab(Scene scene, Function<Scene, String> function) {
        String name = function.apply(scene);
        Prefab prefab = scene.dumpToPrefab();
        scene.getEngine().getAssetsManager().addAsset(name, Prefab.class, prefab);
    }
}
