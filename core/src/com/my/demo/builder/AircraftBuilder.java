package com.my.demo.builder;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.AircraftController;
import com.my.demo.script.AircraftScript;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Prefab;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.gltf.render.GLTFModel;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.physics.force.ConstantForce;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.physics.rigidbody.ConeBody;

import java.util.HashMap;

public class AircraftBuilder {

    public static void initAssets(Engine engine, Scene scene) {
        scene.createPrefab(AircraftBuilder::createBody);
        scene.createPrefab(AircraftBuilder::createWing);
        scene.createPrefab(AircraftBuilder::createEngine);
        scene.createPrefab(AircraftBuilder::createAircraft);
    }

    public static String createAircraft(Scene scene) {

        // Aircraft Entity
        Entity entity = new Entity();
        entity.setName("Aircraft");
        entity.addComponent(new Position(new Matrix4()));
        AircraftScript aircraftScript = entity.addComponent(new AircraftScript());
        aircraftScript.bulletPrefab = scene.getAsset("Bullet", Prefab.class);
        aircraftScript.bombPrefab = scene.getAsset("Bomb", Prefab.class);
        scene.addEntity(entity);

        // Body
        Entity body = scene.instantiatePrefab("Body", new HashMap<String, Object>() {{
            put("Body.config.components[0].config.localTransform", new Matrix4().translate(0, 0.5f, -3));
            put("Body.config.parent", entity);
            put("Body.config.name", "body");
        }});
        Entity engine = scene.instantiatePrefab("Engine", new HashMap<String, Object>() {{
            put("Engine.config.components[0].config.localTransform", new Matrix4().translate(0, 0.6f, -6).rotate(Vector3.X, -90));
            put("Engine.config.components[3].config.base", body);
            put("Engine.config.parent", entity);
            put("Engine.config.name", "engine");
        }});

        // Left
        Matrix4 transform_L = new Matrix4().translate(-1, 0.5f, -5).rotate(Vector3.Z, 90);
        Entity rotate_L = scene.instantiatePrefab("Rotate", new HashMap<String, Object>() {{
            put("Rotate.config.components[0].config.localTransform", transform_L);
            put("Rotate.config.components[3].config.base", body);
            put("Rotate.config.components[3].config.frameInA", new Matrix4(body.getComponent(Position.class).getGlobalTransform()).inv().mul(transform_L).rotate(Vector3.X, 90));
            put("Rotate.config.components[4]", new AircraftController(-0.15f, 0.2f, 0.5f));
            put("Rotate.config.parent", entity);
            put("Rotate.config.name", "rotate_L");
        }});
        Entity wing_L1 = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(-2.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.config.components[3].config.base", rotate_L);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_L1");
        }});
        Entity wing_L2 = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(-4.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.config.components[3].config.base", wing_L1);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_L2");
        }});

        // Right
        Matrix4 transform_R = new Matrix4().translate(1, 0.5f, -5).rotate(Vector3.Z, 90);
        Entity rotate_R = scene.instantiatePrefab("Rotate", new HashMap<String, Object>() {{
            put("Rotate.config.components[0].config.localTransform", transform_R);
            put("Rotate.config.components[3].config.base", body);
            put("Rotate.config.components[3].config.frameInA", new Matrix4(body.getComponent(Position.class).getGlobalTransform()).inv().mul(transform_R).rotate(Vector3.X, 90));
            put("Rotate.config.components[4]", new AircraftController(-0.15f, 0.2f, 0.5f));
            put("Rotate.config.parent", entity);
            put("Rotate.config.name", "rotate_R");
        }});
        Entity wing_R1 = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(2.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.config.components[3].config.base", rotate_R);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_R1");
        }});
        Entity wing_R2 = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(4.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.config.components[3].config.base", wing_R1);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_R2");
        }});

        // Horizontal Tail
        Matrix4 transform_T = new Matrix4().translate(0, 0.5f, 0.1f).rotate(Vector3.Z, 90);
        Entity rotate_T = scene.instantiatePrefab("Rotate", new HashMap<String, Object>() {{
            put("Rotate.config.components[0].config.localTransform", transform_T);
            put("Rotate.config.components[3].config.base", body);
            put("Rotate.config.components[3].config.frameInA", new Matrix4(body.getComponent(Position.class).getGlobalTransform()).inv().mul(transform_T).rotate(Vector3.X, 90));
            put("Rotate.config.components[4]", new AircraftController(-0.2f, 0.2f, 1f));
            put("Rotate.config.parent", entity);
            put("Rotate.config.name", "rotate_T");
        }});
        Entity wing_TL = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(-1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f));
            put("Wing.config.components[3].config.base", rotate_T);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_TL");
        }});
        Entity wing_TR = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f));
            put("Wing.config.components[3].config.base", rotate_T);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_TR");
        }});


        // Vertical Tail
        Matrix4 transform_VL = new Matrix4().translate(-0.6f, 1f, -1).rotate(Vector3.Z, 90);
        Entity wing_VL = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", transform_VL);
            put("Wing.config.components[3]", new HingeConstraint(
                    body,
                    new Matrix4(body.getComponent(Position.class).getGlobalTransform()).inv().mul(transform_VL).translate(0, -0.1f, -0.5f).rotate(Vector3.Y, 90),
                    new Matrix4().translate(0, -0.1f, -0.5f).rotate(Vector3.Y, 90),
                    false
            ));
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_VL");
        }});
        wing_VL.addComponent(new AircraftController(0, 0.2f, 1f));
        Matrix4 transform_VR = new Matrix4().translate(0.6f, 1f, -1).rotate(Vector3.Z, 90);
        Entity wing_VR = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", transform_VR);
            put("Wing.config.components[3]", new HingeConstraint(
                    body,
                    new Matrix4(body.getComponent(Position.class).getGlobalTransform()).inv().mul(transform_VR).translate(0, 0.1f, -0.5f).rotate(Vector3.Y, 90),
                    new Matrix4().translate(0, 0.1f, -0.5f).rotate(Vector3.Y, 90),
                    false
            ));
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_VR");
        }});
        wing_VR.addComponent(new AircraftController(-0.2f, 0, 1f));

        return "Aircraft";
    }

    public static String createBody(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Body");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModel("obj/body.gltf"));
        entity.addComponent(new BoxBody(new Vector3(0.5f,0.5f,2.5f), 50f));
        entity.addComponent(new DragForce(new Vector3(0, 0, 1.2f), new Vector3(), false));

        scene.addEntity(entity);
        return "Body";
    }

    public static String createWing(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Wing");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModel("obj/wing.gltf"));
        entity.addComponent(new BoxBody(new Vector3(1f,0.1f,0.5f), 25f));
        entity.addComponent(new ConnectConstraint(scene.tmpEntity(), 500));
        entity.addComponent(new DragForce(new Vector3(0, 30, 0), new Vector3(), false));

        scene.addEntity(entity);
        return "Wing";
    }

    public static final float force = 4000;
    public static String createEngine(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Engine");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModel("obj/engine.gltf"));
        entity.addComponent(new ConeBody(0.45f,1, 50));
        entity.addComponent(new ConnectConstraint(scene.tmpEntity(), 2000));
        entity.addComponent(new ConstantForce(new Vector3(0, force, 0), new Vector3(), false));

        scene.addEntity(entity);
        return "Engine";
    }
}
