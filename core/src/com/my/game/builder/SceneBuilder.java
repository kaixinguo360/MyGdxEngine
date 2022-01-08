package com.my.game.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.game.script.ExitScript;
import com.my.game.script.GUIScript;
import com.my.game.script.GunScript;
import com.my.game.script.ReloadScript;
import com.my.world.core.*;
import com.my.world.module.common.Position;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.Camera;
import com.my.world.module.render.CameraSystem;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.Render;
import com.my.world.module.render.model.Box;
import com.my.world.module.render.model.ExternalModel;

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
        AssetsManager assetsManager = engine.getAssetsManager();
        AircraftBuilder.initAssets(assetsManager);
        GunBuilder.initAssets(assetsManager);
        ObjectBuilder.initAssets(assetsManager);
        BulletBuilder.initAssets(assetsManager);
        SceneBuilder.initAssets(assetsManager);
        PrefabBuilder.initAssets(engine);
    }

    public static void initAssets(AssetsManager assetsManager) {

        // ----- Init Models ----- //
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        // ----- Init Configs ----- //
        assetsManager.addAsset("sky", ModelRender.class, new ExternalModel("obj/sky.g3db"));
        assetsManager.getAsset("sky", ModelRender.class).model.nodes.get(0).scale.scl(20);
        assetsManager.addAsset("ground", ModelRender.class, new Box(10000f, 0.01f, 20000f, Color.WHITE, attributes));

        assetsManager.addAsset("ground", TemplateRigidBody.class, new BoxBody(new Vector3(5000,0.005f,10000), 0f));
    }
}
