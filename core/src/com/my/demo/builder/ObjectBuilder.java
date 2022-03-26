package com.my.demo.builder;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.GunController;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.camera.Camera;
import com.my.world.module.camera.script.EnhancedThirdPersonCameraController;
import com.my.world.module.common.Position;
import com.my.world.module.gltf.render.GLTFModel;
import com.my.world.module.gltf.render.GLTFModelInstance;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.physics.rigidbody.CylinderBody;

import java.util.HashMap;

public class ObjectBuilder {

    public static void initAssets(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();

        assetsManager.addAsset("box", GLTFModel.class, new GLTFModel("obj/box.gltf"));
        assetsManager.addAsset("brick", GLTFModel.class, new GLTFModel("obj/brick.gltf"));
        assetsManager.addAsset("ground", GLTFModel.class, new GLTFModel("obj/ground.gltf"));

        assetsManager.addAsset("box", TemplateRigidBody.class, new BoxBody(new Vector3(0.5f,0.5f,0.5f), 50f));
        assetsManager.addAsset("brick", TemplateRigidBody.class, new BoxBody(new Vector3(1,0.5f,0.5f), 50f));
        assetsManager.addAsset("ground", TemplateRigidBody.class, new BoxBody(new Vector3(5000,0.005f,10000), 0f));

        scene.createPrefab(ObjectBuilder::createBox);
        scene.createPrefab(ObjectBuilder::createRunway);
        scene.createPrefab(ObjectBuilder::createWall);
        scene.createPrefab(ObjectBuilder::createTower);
        scene.createPrefab(ObjectBuilder::createRotate);
        scene.createPrefab(ObjectBuilder::createCamera);
    }

    public static String createRunway(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Runway");
        entity.addComponent(new Position(new Matrix4()));
        scene.addEntity(entity);
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            scene.instantiatePrefab("Box", new HashMap<String, Object>() {{
                put("Box.config.components[0].config.localTransform", new Matrix4().translate(10, 0.5f, -10 * finalI));
                put("Box.config.parent", entity);
            }});
            scene.instantiatePrefab("Box", new HashMap<String, Object>() {{
                put("Box.config.components[0].config.localTransform", new Matrix4().translate(-10, 0.5f, -10 * finalI));
                put("Box.config.parent", entity);
            }});
        }
        return "Runway";
    }

    public static String createTower(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Tower");
        entity.addComponent(new Position(new Matrix4()));
        scene.addEntity(entity);
        scene.instantiatePrefab("Wall", new HashMap<String, Object>() {{
            put("Wall.config.components[0].config.localTransform", new Matrix4());
            put("Wall.config.parent", entity);
            put("Wall.config.name", "Tower-1");
        }});
        scene.instantiatePrefab("Wall", new HashMap<String, Object>() {{
            put("Wall.config.components[0].config.localTransform", new Matrix4().translate(0, 0, 10).rotate(Vector3.Y, 90));
            put("Wall.config.parent", entity);
            put("Wall.config.name", "Tower-2");
        }});
        scene.instantiatePrefab("Wall", new HashMap<String, Object>() {{
            put("Wall.config.components[0].config.localTransform", new Matrix4().translate(10, 0, 10).rotate(Vector3.Y, 180));
            put("Wall.config.parent", entity);
            put("Wall.config.name", "Tower-3");
        }});
        scene.instantiatePrefab("Wall", new HashMap<String, Object>() {{
            put("Wall.config.components[0].config.localTransform", new Matrix4().translate(10, 0, 0).rotate(Vector3.Y, 270));
            put("Wall.config.parent", entity);
            put("Wall.config.name", "Tower-4");
        }});
        return "Tower";
    }

    private static final int height = 5;
    public static String createWall(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Wall");
        entity.addComponent(new Position(new Matrix4()));
        scene.addEntity(entity);
        Matrix4 tmpM = Matrix4Pool.obtain();
        for (int i = 0; i < height; i++) {
            float tmp = 0.5f + (i % 2);
            for (int j = 0; j < 10; j+=2) {
                Entity entity1 = new Entity();
                entity1.setName("Brick");
                entity1.setParent(entity);
                entity1.addComponent(new Position(new Matrix4().setToTranslation(tmp + j, 0.5f + i, 0)));
                entity1.addComponent(new GLTFModelInstance(scene.getAsset("brick", GLTFModel.class)));
                entity1.addComponent(new PresetTemplateRigidBody(scene.getAsset("brick", TemplateRigidBody.class)));
                scene.addEntity(entity1);
            }
        }
        Matrix4Pool.free(tmpM);
        return "Wall";
    }

    public static String createBox(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Box");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModelInstance(scene.getAsset("box", GLTFModel.class)));
        entity.addComponent(new PresetTemplateRigidBody(scene.getAsset("box", TemplateRigidBody.class)));
        scene.addEntity(entity);
        return "Box";
    }

    public static String createRotate(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Rotate");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModel("obj/rotate.gltf"));
        entity.addComponent(new CylinderBody(new Vector3(0.5f,0.5f,0.5f), 50f));
        entity.addComponent(
                new HingeConstraint(
                        scene.tmpEntity(),
                        new Matrix4().rotate(Vector3.X, 90),
                        new Matrix4().rotate(Vector3.X, 90),
                        false
                )
        );
        entity.addComponent(new GunController());

        scene.addEntity(entity);
        return "Rotate";
    }

    public static String createCamera(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Camera");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModel("obj/camera.gltf")).setActive(false);
        entity.addComponent(new Camera(0, 0, 1, 1, 0));
        EnhancedThirdPersonCameraController cameraController = entity.addComponent(new EnhancedThirdPersonCameraController());
        cameraController.centerTarget.set(0, 0, 0);
        cameraController.translateTarget.set(0, 0, 20);
        cameraController.translate.set(0, 0, 20);
        cameraController.recoverRate = 2f;
        cameraController.waitTime = 10;

        scene.addEntity(entity);
        return "Camera";
    }
}
