package com.my.demo.builder.gun;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.PrefabBuilder;
import com.my.demo.builder.object.RotateBuilder;
import com.my.demo.builder.weapon.BombBuilder;
import com.my.demo.builder.weapon.BulletBuilder;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;

import java.util.HashMap;

public class GunBuilder extends PrefabBuilder<GunBuilder> {

    {
        prefabName = "Gun";
    }

    public BulletBuilder bulletBuilder;
    public BombBuilder bombBuilder;
    public RotateBuilder rotateBuilder;
    public BarrelBuilder barrelBuilder;

    @Override
    protected void initDependencies() {
        bulletBuilder = getDependency(BulletBuilder.class);
        bombBuilder = getDependency(BombBuilder.class);
        rotateBuilder = getDependency(RotateBuilder.class);
        barrelBuilder = getDependency(BarrelBuilder.class);
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Gun");
        entity.addComponent(new Position(new Matrix4()));
        GunScript gunScript = entity.addComponent(new GunScript());
        gunScript.bulletPrefab = bulletBuilder.prefab;
        gunScript.bombPrefab = bombBuilder.prefab;
        scene.addEntity(entity);

        Entity rotate_Y = rotateBuilder.build(scene, new HashMap<String, Object>() {{
            put("Rotate.config.components[0].config.localTransform", new Matrix4().translate(0, 0.5f, 0));
            put("Rotate.config.components[3]", null);
            put("Rotate.config.parent", entity);
            put("Rotate.config.name", "rotate_Y");
        }});
        Matrix4 transform = new Matrix4().translate(0, 1.5f, 0).rotate(Vector3.Z, 90);
        Entity rotate_X = rotateBuilder.build(scene, new HashMap<String, Object>() {{
            put("Rotate.config.components[0].config.localTransform", transform);
            put("Rotate.config.components[3].config.base", rotate_Y);
            put("Rotate.config.components[3].config.frameInA", rotate_Y.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform).rotate(Vector3.X, 90));
            put("Rotate.config.components[4].config.min", (float) Math.toRadians(-90));
            put("Rotate.config.components[4].config.max", (float) Math.toRadians(0));
            put("Rotate.config.components[4].config.limit", true);
            put("Rotate.config.parent", entity);
            put("Rotate.config.name", "rotate_X");
        }});
        barrelBuilder.build(scene, new HashMap<String, Object>() {{
            put("Barrel.config.components[0].config.localTransform", new Matrix4().translate(0, 1.5f, -3));
            put("Barrel.config.components[3].config.base", rotate_X);
            put("Barrel.config.parent", entity);
            put("Barrel.config.name", "barrel");
        }});
    }
}
