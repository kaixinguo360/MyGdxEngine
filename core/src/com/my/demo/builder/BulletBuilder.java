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
import com.my.world.module.render.model.Capsule;

public class BulletBuilder extends BaseBuilder {

    public static void initAssets(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();

        assetsManager.addAsset("bullet", ModelRender.class, new Capsule(0.5f, 2, 8, Color.YELLOW, VertexAttributes.Usage.Position));
        assetsManager.addAsset("bomb", ModelRender.class, new Capsule(0.5f, 2, 8, Color.GRAY, attributes));

        assetsManager.addAsset("bullet", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        assetsManager.addAsset("bomb", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        assetsManager.addAsset("explosion", TemplateRigidBody.class, new SphereBody(30, 50f));

        SceneBuilder.createPrefab(scene, BulletBuilder::createExplosion);
        SceneBuilder.createPrefab(scene, BulletBuilder::createBomb);
        SceneBuilder.createPrefab(scene, BulletBuilder::createBullet);
    }

    public static String createExplosion(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Explosion");
        entity.addComponent(new Position(new Matrix4()));
        TemplateRigidBody templateRigidBody = getAsset(scene, "explosion", TemplateRigidBody.class);
        entity.addComponent(new PresetTemplateRigidBody(templateRigidBody, true));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new ExplosionScript());
        addEntity(scene, entity);
        return "Explosion";
    }

    public static String createBullet(Scene scene) {
        Entity entity = createEntity(scene, "bullet");
        entity.setName("Bullet");
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        addEntity(scene, entity);
        return "Bullet";
    }

    public static String createBomb(Scene scene) {
        Entity entity = createEntity(scene, "bomb");
        entity.setName("Bomb");
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        entity.addComponent(new BombScript()).explosionPrefab = getAsset(scene, "Explosion", Prefab.class);
        entity.addComponent(new DragForce(new Vector3(0, 0, 0.05f), new Vector3(0, -1, 0), false));
        entity.addComponent(new DragForce(new Vector3(0.05f, 0, 0), new Vector3(0, -1, 0), false));
        addEntity(scene, entity);
        return "Bomb";
    }
}
