package com.my.demo.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.GunScript;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Prefab;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.Box;

import java.util.HashMap;

public class GunBuilder extends BaseBuilder {

    public static void initAssets(Engine engine, Scene scene) {
        SceneBuilder.createPrefab(scene, GunBuilder::createBarrel);
        SceneBuilder.createPrefab(scene, GunBuilder::createGun);
    }

    public static String createGun(Scene scene) {

        Entity entity = new Entity();
        entity.setName("Gun");
        entity.addComponent(new Position(new Matrix4()));
        GunScript gunScript = entity.addComponent(new GunScript());
        gunScript.bulletPrefab = getAsset(scene, "Bullet", Prefab.class);
        gunScript.bombPrefab = getAsset(scene, "Bomb", Prefab.class);
        addEntity(scene, entity);

        Entity rotate_Y = scene.instantiatePrefab("Rotate", new HashMap<String, Object>() {{
            put("Rotate.components[0].config.localTransform", new Matrix4().translate(0, 0.5f, 0));
            put("Rotate.components[3]", null);
            put("Rotate.parent", entity);
            put("Rotate.name", "rotate_Y");
        }});
        Matrix4 transform = new Matrix4().translate(0, 1.5f, 0).rotate(Vector3.Z, 90);
        Entity rotate_X = scene.instantiatePrefab("Rotate", new HashMap<String, Object>() {{
            put("Rotate.components[0].config.localTransform", transform);
            put("Rotate.components[3].config.base", rotate_Y);
            put("Rotate.components[3].config.frameInA", new Matrix4(rotate_Y.getComponent(Position.class).getLocalTransform()).inv().mul(transform).rotate(Vector3.X, 90));
            put("Rotate.components[4].config.min", (float) Math.toRadians(-90));
            put("Rotate.components[4].config.max", (float) Math.toRadians(0));
            put("Rotate.components[4].config.limit", true);
            put("Rotate.parent", entity);
            put("Rotate.name", "rotate_X");
        }});
        scene.instantiatePrefab("Barrel", new HashMap<String, Object>() {{
            put("Barrel.components[0].config.localTransform", new Matrix4().translate(0, 1.5f, -3));
            put("Barrel.components[3].config.base", rotate_X);
            put("Barrel.parent", entity);
            put("Barrel.name", "barrel");
        }});

        return "Gun";
    }

    public static String createBarrel(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Barrel");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new Box(1, 1, 5, Color.GREEN, attributes));
        entity.addComponent(new BoxBody(new Vector3(0.5f,0.5f,2.5f), 5f));
        entity.addComponent(new ConnectConstraint(tmpEntity(scene), 2000));

        addEntity(scene, entity);
        return "Barrel";
    }
}
