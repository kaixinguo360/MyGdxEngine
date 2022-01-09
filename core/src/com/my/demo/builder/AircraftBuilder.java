package com.my.demo.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.AircraftController;
import com.my.demo.script.AircraftScript;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Prefab;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.force.ConstantForce;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.physics.rigidbody.ConeBody;
import com.my.world.module.render.model.Box;
import com.my.world.module.render.model.Cone;

import java.util.HashMap;

public class AircraftBuilder extends BaseBuilder {

    public static void initAssets(Engine engine, Scene scene) {
        SceneBuilder.createPrefab(scene, AircraftBuilder::createBody);
        SceneBuilder.createPrefab(scene, AircraftBuilder::createWing);
        SceneBuilder.createPrefab(scene, AircraftBuilder::createEngine);
        SceneBuilder.createPrefab(scene, AircraftBuilder::createAircraft);
    }

    public static String createAircraft(Scene scene) {

        // Aircraft Entity
        Entity entity = new Entity();
        entity.setName("Aircraft");
        entity.addComponent(new Position(new Matrix4()));
        AircraftScript aircraftScript = entity.addComponent(new AircraftScript());
        aircraftScript.bulletPrefab = getAsset(scene, "Bullet", Prefab.class);
        aircraftScript.bombPrefab = getAsset(scene, "Bomb", Prefab.class);
        addEntity(scene, entity);

        // Body
        Entity body = scene.instantiatePrefab("Body", new HashMap<String, Object>() {{
            put("Body.components[0].config.localTransform", new Matrix4().translate(0, 0.5f, -3));
            put("Body.parent", entity);
            put("Body.name", "body");
        }});
        Entity engine = scene.instantiatePrefab("Engine", new HashMap<String, Object>() {{
            put("Engine.components[0].config.localTransform", new Matrix4().translate(0, 0.6f, -6).rotate(Vector3.X, -90));
            put("Engine.components[3].config.base", body);
            put("Engine.parent", entity);
            put("Engine.name", "engine");
        }});

        // Left
        Matrix4 transform_L = new Matrix4().translate(-1, 0.5f, -5).rotate(Vector3.Z, 90);
        Entity rotate_L = scene.instantiatePrefab("Rotate", new HashMap<String, Object>() {{
            put("Rotate.components[0].config.localTransform", transform_L);
            put("Rotate.components[3].config.base", body);
            put("Rotate.components[3].config.frameInA", new Matrix4(body.getComponent(Position.class).getLocalTransform()).inv().mul(transform_L).rotate(Vector3.X, 90));
            put("Rotate.components[4]", new AircraftController(-0.15f, 0.2f, 0.5f));
            put("Rotate.parent", entity);
            put("Rotate.name", "rotate_L");
        }});
        Entity wing_L1 = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.components[0].config.localTransform", new Matrix4().translate(-2.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.components[3].config.base", rotate_L);
            put("Wing.parent", entity);
            put("Wing.name", "wing_L1");
        }});
        Entity wing_L2 = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.components[0].config.localTransform", new Matrix4().translate(-4.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.components[3].config.base", wing_L1);
            put("Wing.parent", entity);
            put("Wing.name", "wing_L2");
        }});

        // Right
        Matrix4 transform_R = new Matrix4().translate(1, 0.5f, -5).rotate(Vector3.Z, 90);
        Entity rotate_R = scene.instantiatePrefab("Rotate", new HashMap<String, Object>() {{
            put("Rotate.components[0].config.localTransform", transform_R);
            put("Rotate.components[3].config.base", body);
            put("Rotate.components[3].config.frameInA", new Matrix4(body.getComponent(Position.class).getLocalTransform()).inv().mul(transform_R).rotate(Vector3.X, 90));
            put("Rotate.components[4]", new AircraftController(-0.15f, 0.2f, 0.5f));
            put("Rotate.parent", entity);
            put("Rotate.name", "rotate_R");
        }});
        Entity wing_R1 = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.components[0].config.localTransform", new Matrix4().translate(2.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.components[3].config.base", rotate_R);
            put("Wing.parent", entity);
            put("Wing.name", "wing_R1");
        }});
        Entity wing_R2 = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.components[0].config.localTransform", new Matrix4().translate(4.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.components[3].config.base", wing_R1);
            put("Wing.parent", entity);
            put("Wing.name", "wing_R2");
        }});

        // Horizontal Tail
        Matrix4 transform_T = new Matrix4().translate(0, 0.5f, 0.1f).rotate(Vector3.Z, 90);
        Entity rotate_T = scene.instantiatePrefab("Rotate", new HashMap<String, Object>() {{
            put("Rotate.components[0].config.localTransform", transform_T);
            put("Rotate.components[3].config.base", body);
            put("Rotate.components[3].config.frameInA", new Matrix4(body.getComponent(Position.class).getLocalTransform()).inv().mul(transform_T).rotate(Vector3.X, 90));
            put("Rotate.components[4]", new AircraftController(-0.2f, 0.2f, 1f));
            put("Rotate.parent", entity);
            put("Rotate.name", "rotate_T");
        }});
        Entity wing_TL = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.components[0].config.localTransform", new Matrix4().translate(-1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f));
            put("Wing.components[3].config.base", rotate_T);
            put("Wing.parent", entity);
            put("Wing.name", "wing_TL");
        }});
        Entity wing_TR = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.components[0].config.localTransform", new Matrix4().translate(1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f));
            put("Wing.components[3].config.base", rotate_T);
            put("Wing.parent", entity);
            put("Wing.name", "wing_TR");
        }});

        // Vertical Tail
        Entity wing_VL = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.components[0].config.localTransform", new Matrix4().translate(-0.6f, 1f, -1).rotate(Vector3.Z, 90));
            put("Wing.components[3].config.base", body);
            put("Wing.parent", entity);
            put("Wing.name", "wing_VL");
        }});
        Entity wing_VR = scene.instantiatePrefab("Wing", new HashMap<String, Object>() {{
            put("Wing.components[0].config.localTransform", new Matrix4().translate(0.6f, 1f, -1).rotate(Vector3.Z, 90));
            put("Wing.components[3].config.base", body);
            put("Wing.parent", entity);
            put("Wing.name", "wing_VR");
        }});

        return "Aircraft";
    }

    private static String createBody(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Body");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new Box(1, 1, 5, Color.GREEN, attributes));
        entity.addComponent(new BoxBody(new Vector3(0.5f,0.5f,2.5f), 50f));
        entity.addComponent(new DragForce(new Vector3(0, 0, 1.2f), new Vector3(), false));

        addEntity(scene, entity);
        return "Body";
    }

    private static String createWing(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Wing");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new Box(2, 0.2f, 1, Color.BLUE, attributes));
        entity.addComponent(new BoxBody(new Vector3(1f,0.1f,0.5f), 25f));
        entity.addComponent(new ConnectConstraint(tmpEntity(scene), 500));
        entity.addComponent(new DragForce(new Vector3(0, 30, 0), new Vector3(), false));

        addEntity(scene, entity);
        return "Wing";
    }

    private static final float force = 4000;
    private static String createEngine(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Engine");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new Cone(0.9f, 1, 0.9f, 18, Color.YELLOW, attributes));
        entity.addComponent(new ConeBody(0.45f,1, 50));
        entity.addComponent(new ConnectConstraint(tmpEntity(scene), 2000));
        entity.addComponent(new ConstantForce(new Vector3(0, force, 0), new Vector3(), false));

        addEntity(scene, entity);
        return "Engine";
    }
}
