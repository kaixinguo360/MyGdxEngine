package com.my.demo.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.BombScript;
import com.my.demo.script.ExplosionScript;
import com.my.demo.script.RemoveScript;
import com.my.world.core.*;
import com.my.world.module.common.Position;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.CapsuleBody;
import com.my.world.module.physics.rigidbody.SphereBody;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.PresetModelRender;
import com.my.world.module.render.model.Capsule;

import static com.my.demo.builder.SceneBuilder.attributes;

public class BulletBuilder {

    public static void initAssets(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();

        assetsManager.addAsset("bullet", ModelRender.class, new Capsule(0.5f, 2, 8, Color.YELLOW, VertexAttributes.Usage.Position));
        assetsManager.addAsset("bomb", ModelRender.class, new Capsule(0.5f, 2, 8, Color.GRAY, attributes));

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
        entity.addComponent(new PresetModelRender(scene.getAsset("bullet", ModelRender.class)));
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
        entity.addComponent(new PresetModelRender(scene.getAsset("bomb", ModelRender.class)));
        entity.addComponent(new PresetTemplateRigidBody(scene.getAsset("bomb", TemplateRigidBody.class)));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        entity.addComponent(new BombScript()).explosionPrefab = scene.getAsset("Explosion", Prefab.class);
        entity.addComponent(new DragForce(new Vector3(0, 0, 0.05f), new Vector3(0, -1, 0), false));
        entity.addComponent(new DragForce(new Vector3(0.05f, 0, 0), new Vector3(0, -1, 0), false));
        scene.addEntity(entity);
        return "Bomb";
    }
}
