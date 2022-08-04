package com.my.demo.builder.aircraft;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.object.RotateBuilder;
import com.my.demo.builder.weapon.BombBuilder;
import com.my.demo.builder.weapon.BulletBuilder;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.EntityBuilder;
import com.my.world.enhanced.builder.PrefabBuilder;
import com.my.world.module.common.Position;
import com.my.world.module.physics.constraint.HingeConstraint;

import java.util.HashMap;

public class AircraftBuilder extends PrefabBuilder<AircraftBuilder> {

    {
        prefabName = "Aircraft";
    }

    public BulletBuilder bulletBuilder;
    public BombBuilder bombBuilder;
    public EntityBuilder bodyBuilder;
    public EntityBuilder engineBuilder;
    public EntityBuilder wingBuilder;
    public EntityBuilder rotateBuilder;

    @Override
    protected void initDependencies() {
        bulletBuilder = getDependency(BulletBuilder.class);
        bombBuilder = getDependency(BombBuilder.class);
        bodyBuilder = getDependency(BodyBuilder.class);
        engineBuilder = getDependency(EngineBuilder.class);
        wingBuilder = getDependency(WingBuilder.class);
        rotateBuilder = getDependency(RotateBuilder.class);
    }

    @Override
    public void createPrefab(Scene scene) {

        // Aircraft Entity
        Entity entity = new Entity();
        entity.setName("Aircraft");
        entity.addComponent(new Position(new Matrix4()));
        AircraftScript aircraftScript = entity.addComponent(new AircraftScript());
        aircraftScript.bulletPrefab = bulletBuilder.prefab;
        aircraftScript.bombPrefab = bombBuilder.prefab;
        scene.addEntity(entity);

        // Body
        Entity body = bodyBuilder.build(scene, new HashMap<String, Object>() {{
            put("Body.config.components[0].config.localTransform", new Matrix4().translate(0, 0.5f, -3));
            put("Body.config.parent", entity);
            put("Body.config.name", "body");
        }});
        Entity engine = engineBuilder.build(scene, new HashMap<String, Object>() {{
            put("Engine.config.components[0].config.localTransform", new Matrix4().translate(0, 0.6f, -6).rotate(Vector3.X, -90));
            put("Engine.config.components[3].config.base", body);
            put("Engine.config.parent", entity);
            put("Engine.config.name", "engine");
        }});

        // Left
        Matrix4 transform_L = new Matrix4().translate(-1, 0.5f, -5).rotate(Vector3.Z, 90);
        Entity rotate_L = rotateBuilder.build(scene, new HashMap<String, Object>() {{
            put("Rotate.config.components[0].config.localTransform", transform_L);
            put("Rotate.config.components[3].config.base", body);
            put("Rotate.config.components[3].config.frameInA", body.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform_L).rotate(Vector3.X, 90));
            put("Rotate.config.components[4]", new AircraftController(-0.15f, 0.2f, 0.5f));
            put("Rotate.config.parent", entity);
            put("Rotate.config.name", "rotate_L");
        }});
        Entity wing_L1 = wingBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(-2.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.config.components[3].config.base", rotate_L);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_L1");
        }});
        Entity wing_L2 = wingBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(-4.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.config.components[3].config.base", wing_L1);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_L2");
        }});

        // Right
        Matrix4 transform_R = new Matrix4().translate(1, 0.5f, -5).rotate(Vector3.Z, 90);
        Entity rotate_R = rotateBuilder.build(scene, new HashMap<String, Object>() {{
            put("Rotate.config.components[0].config.localTransform", transform_R);
            put("Rotate.config.components[3].config.base", body);
            put("Rotate.config.components[3].config.frameInA", body.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform_R).rotate(Vector3.X, 90));
            put("Rotate.config.components[4]", new AircraftController(-0.15f, 0.2f, 0.5f));
            put("Rotate.config.parent", entity);
            put("Rotate.config.name", "rotate_R");
        }});
        Entity wing_R1 = wingBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(2.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.config.components[3].config.base", rotate_R);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_R1");
        }});
        Entity wing_R2 = wingBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(4.5f, 0.5f, -5).rotate(Vector3.X, 14));
            put("Wing.config.components[3].config.base", wing_R1);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_R2");
        }});

        // Horizontal Tail
        Matrix4 transform_T = new Matrix4().translate(0, 0.5f, 0.1f).rotate(Vector3.Z, 90);
        Entity rotate_T = rotateBuilder.build(scene, new HashMap<String, Object>() {{
            put("Rotate.config.components[0].config.localTransform", transform_T);
            put("Rotate.config.components[3].config.base", body);
            put("Rotate.config.components[3].config.frameInA", body.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform_T).rotate(Vector3.X, 90));
            put("Rotate.config.components[4]", new AircraftController(-0.2f, 0.2f, 1f));
            put("Rotate.config.parent", entity);
            put("Rotate.config.name", "rotate_T");
        }});
        Entity wing_TL = wingBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(-1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f));
            put("Wing.config.components[3].config.base", rotate_T);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_TL");
        }});
        Entity wing_TR = wingBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", new Matrix4().translate(1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f));
            put("Wing.config.components[3].config.base", rotate_T);
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_TR");
        }});


        // Vertical Tail
        Matrix4 transform_VL = new Matrix4().translate(-0.6f, 1f, -1).rotate(Vector3.Z, 90);
        Entity wing_VL = wingBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", transform_VL);
            put("Wing.config.components[3]", new HingeConstraint(
                    body,
                    body.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform_VL).translate(0, -0.1f, -0.5f).rotate(Vector3.Y, 90),
                    new Matrix4().translate(0, -0.1f, -0.5f).rotate(Vector3.Y, 90),
                    false
            ));
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_VL");
        }});
        wing_VL.addComponent(new AircraftController(0, 0.2f, 1f));
        Matrix4 transform_VR = new Matrix4().translate(0.6f, 1f, -1).rotate(Vector3.Z, 90);
        Entity wing_VR = wingBuilder.build(scene, new HashMap<String, Object>() {{
            put("Wing.config.components[0].config.localTransform", transform_VR);
            put("Wing.config.components[3]", new HingeConstraint(
                    body,
                    body.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform_VR).translate(0, 0.1f, -0.5f).rotate(Vector3.Y, 90),
                    new Matrix4().translate(0, 0.1f, -0.5f).rotate(Vector3.Y, 90),
                    false
            ));
            put("Wing.config.parent", entity);
            put("Wing.config.name", "wing_VR");
        }});
        wing_VR.addComponent(new AircraftController(-0.2f, 0, 1f));
    }
}
