package com.my.game.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.my.game.constraint.ConnectConstraint;
import com.my.game.model.CapsuleModel;
import com.my.game.rigidbody.CapsuleConfig;
import com.my.game.script.BombScript;
import com.my.game.script.ExplosionScript;
import com.my.game.script.RemoveScript;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityManager;
import com.my.utils.world.Prefab;
import com.my.utils.world.com.*;

public class BulletBuilder extends BaseBuilder {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        assetsManager.addAsset("bullet", RenderModel.class, new CapsuleModel(0.5f, 2, 8, Color.YELLOW, VertexAttributes.Usage.Position));
        assetsManager.addAsset("bomb", RenderModel.class, new CapsuleModel(0.5f, 2, 8, Color.GRAY, attributes));
        assetsManager.addAsset("explosion", btCollisionShape.class, new btSphereShape(30));

        assetsManager.addAsset("bullet", RigidBodyConfig.class, new CapsuleConfig(0.5f, 1, 50f));
        assetsManager.addAsset("bomb", RigidBodyConfig.class, new CapsuleConfig(0.5f, 1, 50f));
    }

    public BulletBuilder(AssetsManager assetsManager, EntityManager entityManager) {
        super(assetsManager, entityManager);
    }

    public Entity createExplosion(String name, Matrix4 transform) {
        Entity entity = new Entity();
        entity.setName(name);
        entity.addComponent(new Position(new Matrix4(transform)));
        btCollisionShape shape = assetsManager.getAsset("explosion", btCollisionShape.class);
        entity.addComponent(new Collider(shape));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new ExplosionScript());
        return entity;
    }

    public Entity createBullet(String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity("bullet");
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 2000));
        }
        return addEntity(name, transform, entity);
    }

    public Entity createBomb(String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity("bomb");
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveScript());
        entity.addComponent(new BombScript()).explosionPrefab = assetsManager.getAsset("Explosion", Prefab.class);
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 2000));
        }
        return addEntity(name, transform, entity);
    }
}
