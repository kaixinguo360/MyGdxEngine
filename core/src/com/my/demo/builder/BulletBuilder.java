package com.my.demo.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.BombScript;
import com.my.demo.script.ExplosionScript;
import com.my.demo.script.RemoveScript;
import com.my.world.core.*;
import com.my.world.module.common.Position;
import com.my.world.module.gltf.light.GLTFPointLight;
import com.my.world.module.gltf.render.GLTFModel;
import com.my.world.module.gltf.render.GLTFModelInstance;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.CapsuleBody;
import com.my.world.module.physics.rigidbody.SphereBody;

public class BulletBuilder {

    public static void initAssets(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();

        assetsManager.addAsset("bullet", GLTFModel.class, new GLTFModel("obj/bullet.gltf"));
        assetsManager.addAsset("bomb", GLTFModel.class, new GLTFModel("obj/bomb.gltf"));

        assetsManager.addAsset("bullet", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        assetsManager.addAsset("bomb", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        assetsManager.addAsset("sphere", TemplateRigidBody.class, new SphereBody(30, 50f));
        assetsManager.addAsset("sphere1", TemplateRigidBody.class, new SphereBody(5, 50f));

        scene.createPrefab(BulletBuilder::createExplosion);
        scene.createPrefab(BulletBuilder::createBomb);
        scene.createPrefab(BulletBuilder::createBullet);
    }

    public static String createExplosion(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Explosion");
        entity.addComponent(new Position(new Matrix4()));
        TemplateRigidBody templateRigidBody = scene.getAsset("sphere", TemplateRigidBody.class);
        entity.addComponent(new PresetTemplateRigidBody(templateRigidBody, true));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new ExplosionScript());
        scene.addEntity(entity);
        return "Explosion";
    }

    public static String createBullet(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Bullet");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModelInstance(scene.getAsset("bullet", GLTFModel.class)));
        entity.addComponent(new PresetTemplateRigidBody(scene.getAsset("bullet", TemplateRigidBody.class)));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        scene.addEntity(entity);
        return "Bullet";
    }

    public static String createBomb(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Bomb");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModelInstance(scene.getAsset("bomb", GLTFModel.class)));
        entity.addComponent(new PresetTemplateRigidBody(scene.getAsset("bomb", TemplateRigidBody.class)));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        entity.addComponent(new BombScript()).explosionPrefab = scene.getAsset("Explosion", Prefab.class);
        entity.addComponent(new DragForce(new Vector3(0, 0, 0.05f), new Vector3(0, -1, 0), false));
        entity.addComponent(new DragForce(new Vector3(0.05f, 0, 0), new Vector3(0, -1, 0), false));
        entity.addComponent(new GLTFPointLight(Color.YELLOW.cpy(), 20f, 30f));
        scene.addEntity(entity);
        return "Bomb";
    }
}
