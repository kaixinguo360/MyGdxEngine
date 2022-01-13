package com.my.demo.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.GunController;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.physics.rigidbody.CylinderBody;
import com.my.world.module.render.Camera;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.PresetModelRender;
import com.my.world.module.render.model.Box;
import com.my.world.module.render.model.Cylinder;
import com.my.world.module.render.model.ExternalModel;
import com.my.world.module.render.script.EnhancedThirdPersonCameraController;

import java.util.HashMap;

import static com.my.demo.builder.SceneBuilder.attributes;

public class ObjectBuilder {

    public static void initAssets(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();

        assetsManager.addAsset("box", ModelRender.class, new Box(1, 1, 1, Color.RED, attributes));
        assetsManager.addAsset("brick", ModelRender.class, new Box(2, 1, 1, Color.LIGHT_GRAY, attributes));
        assetsManager.addAsset("ground", ModelRender.class, new Box(10000f, 0.01f, 20000f, Color.WHITE, attributes));
        assetsManager.addAsset("sky", ModelRender.class, new ExternalModel("obj/sky.g3db"));
        assetsManager.getAsset("sky", ModelRender.class).model.nodes.get(0).scale.scl(20);

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
                put("Box.components[0].config.localTransform", new Matrix4().translate(10, 0.5f, -10 * finalI));
                put("Box.parent", entity);
            }});
            scene.instantiatePrefab("Box", new HashMap<String, Object>() {{
                put("Box.components[0].config.localTransform", new Matrix4().translate(-10, 0.5f, -10 * finalI));
                put("Box.parent", entity);
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
            put("Wall.components[0].config.localTransform", new Matrix4());
            put("Wall.parent", entity);
            put("Wall.name", "Tower-1");
        }});
        scene.instantiatePrefab("Wall", new HashMap<String, Object>() {{
            put("Wall.components[0].config.localTransform", new Matrix4().translate(0, 0, 10).rotate(Vector3.Y, 90));
            put("Wall.parent", entity);
            put("Wall.name", "Tower-2");
        }});
        scene.instantiatePrefab("Wall", new HashMap<String, Object>() {{
            put("Wall.components[0].config.localTransform", new Matrix4().translate(10, 0, 10).rotate(Vector3.Y, 180));
            put("Wall.parent", entity);
            put("Wall.name", "Tower-3");
        }});
        scene.instantiatePrefab("Wall", new HashMap<String, Object>() {{
            put("Wall.components[0].config.localTransform", new Matrix4().translate(10, 0, 0).rotate(Vector3.Y, 270));
            put("Wall.parent", entity);
            put("Wall.name", "Tower-4");
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
                entity1.addComponent(new PresetModelRender(scene.getAsset("brick", ModelRender.class)));
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
        entity.addComponent(new PresetModelRender(scene.getAsset("box", ModelRender.class)));
        entity.addComponent(new PresetTemplateRigidBody(scene.getAsset("box", TemplateRigidBody.class)));
        scene.addEntity(entity);
        return "Box";
    }

    public static String createRotate(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Rotate");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new Cylinder(1, 1, 1, 8, Color.CYAN, attributes));
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
        entity.addComponent(new Box(1, 1, 1, Color.YELLOW, attributes)).setActive(false);
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
