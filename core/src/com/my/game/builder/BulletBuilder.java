package com.my.game.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.my.game.script.BombScript;
import com.my.game.script.ExplosionScript;
import com.my.game.script.RemoveScript;
import com.my.world.core.AssetsManager;
import com.my.world.core.Entity;
import com.my.world.core.EntityManager;
import com.my.world.core.Prefab;
import com.my.world.module.common.Position;
import com.my.world.module.physics.Collider;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.RigidBodyConfig;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.rigidbody.CapsuleConfig;
import com.my.world.module.physics.rigidbody.SphereConfig;
import com.my.world.module.render.RenderModel;
import com.my.world.module.render.model.CapsuleModel;

public class BulletBuilder extends BaseBuilder {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        assetsManager.addAsset("bullet", RenderModel.class, new CapsuleModel(0.5f, 2, 8, Color.YELLOW, VertexAttributes.Usage.Position));
        assetsManager.addAsset("bomb", RenderModel.class, new CapsuleModel(0.5f, 2, 8, Color.GRAY, attributes));

        assetsManager.addAsset("bullet", RigidBodyConfig.class, new CapsuleConfig(0.5f, 1, 50f));
        assetsManager.addAsset("bomb", RigidBodyConfig.class, new CapsuleConfig(0.5f, 1, 50f));
        assetsManager.addAsset("explosion", RigidBodyConfig.class, new SphereConfig(30, 50f));
    }

    public BulletBuilder(AssetsManager assetsManager, EntityManager entityManager) {
        super(assetsManager, entityManager);
    }

    public Entity createExplosion(String name, Matrix4 transform) {
        Entity entity = new Entity();
        entity.setName(name);
        entity.addComponent(new Position(new Matrix4(transform)));
        RigidBodyConfig rigidBodyConfig = assetsManager.getAsset("explosion", RigidBodyConfig.class);
        entity.addComponent(new Collider(rigidBodyConfig));
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
