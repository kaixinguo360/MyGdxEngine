package com.my.demo.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.my.demo.script.BombScript;
import com.my.demo.script.ExplosionScript;
import com.my.demo.script.RemoveScript;
import com.my.world.core.AssetsManager;
import com.my.world.core.Entity;
import com.my.world.core.Prefab;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.rigidbody.CapsuleBody;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.model.Capsule;

public class BulletBuilder extends BaseBuilder {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        assetsManager.addAsset("bullet", ModelRender.class, new Capsule(0.5f, 2, 8, Color.YELLOW, VertexAttributes.Usage.Position));
        assetsManager.addAsset("bomb", ModelRender.class, new Capsule(0.5f, 2, 8, Color.GRAY, attributes));

        assetsManager.addAsset("bullet", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        assetsManager.addAsset("bomb", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        assetsManager.addAsset("explosion", TemplateRigidBody.class, new SphereBody(30, 50f));
    }

    public static String createExplosion(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Explosion");
        entity.addComponent(new Position(new Matrix4()));
        TemplateRigidBody templateRigidBody = getAsset(scene, "explosion", TemplateRigidBody.class);
        entity.addComponent(new PresetTemplateRigidBody(templateRigidBody, true));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new ExplosionScript());
        addEntity(scene, "Explosion", new Matrix4(), entity);
        return "Explosion";
    }

    public static String createBullet(Scene scene) {
        Entity entity = createEntity(scene, "bullet");
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        addEntity(scene, "Bullet", new Matrix4(), entity);
        return "Bullet";
    }

    public static String createBomb(Scene scene) {
        Entity entity = createEntity(scene, "bomb");
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        entity.addComponent(new BombScript()).explosionPrefab = getAsset(scene, "Explosion", Prefab.class);
        addEntity(scene, "Bomb", new Matrix4(), entity);
        return "Bomb";
    }
}
