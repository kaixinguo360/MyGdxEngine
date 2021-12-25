package com.my.game.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.game.constraint.ConnectConstraint;
import com.my.game.script.BombScript;
import com.my.game.script.ExplosionScript;
import com.my.game.script.RemoveScript;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityManager;
import com.my.utils.world.Prefab;
import com.my.utils.world.com.Collider;
import com.my.utils.world.com.Collision;
import com.my.utils.world.com.Position;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;

public class BulletBuilder extends BaseBuilder {

    public final static short BULLET_FLAG = 1 << 8;
    public final static short ALL_FLAG = -1;

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();

        assetsManager.addAsset("bullet", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(mdBuilder.createCapsule(0.5f, 2, 8, new Material(ColorAttribute.createDiffuse(Color.YELLOW)), VertexAttributes.Usage.Position)));
        assetsManager.addAsset("bomb", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(mdBuilder.createCapsule(0.5f, 2, 8, new Material(ColorAttribute.createDiffuse(Color.GRAY)), attributes)));
        assetsManager.addAsset("explosion", btCollisionShape.class, new btSphereShape(30));

        assetsManager.addAsset("bullet", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btCapsuleShape(0.5f, 1), 50f));
        assetsManager.addAsset("bomb", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btCapsuleShape(0.5f, 1), 50f));
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
        entity.addComponent(new Collision(1 << 9, -1));
        entity.addComponent(new ExplosionScript());
        return entity;
    }

    public Entity createBullet(String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity("bullet");
        entity.addComponent(new Collision(BULLET_FLAG, ALL_FLAG));
        entity.addComponent(new RemoveScript());
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 2000));
        }
        return addEntity(name, transform, entity);
    }

    public Entity createBomb(String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity("bomb");
        entity.addComponent(new Collision(BULLET_FLAG, ALL_FLAG));
        entity.addComponent(new RemoveScript());
        entity.addComponent(new BombScript()).explosionPrefab = assetsManager.getAsset("Explosion", Prefab.class);
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 2000));
        }
        return addEntity(name, transform, entity);
    }
}
