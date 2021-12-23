package com.my.game.builder;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityManager;
import com.my.utils.world.World;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Render;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.sys.RenderSystem;

public class BaseBuilder {

    protected final AssetsManager assetsManager;
    protected final EntityManager entityManager;

    public BaseBuilder(World world) {
        this.assetsManager = world.getAssetsManager();
        this.entityManager = world.getEntityManager();
    }

    public BaseBuilder(AssetsManager assetsManager, EntityManager entityManager) {
        this.assetsManager = assetsManager;
        this.entityManager = entityManager;
    }

    public Entity createEntity(String className) {
        Entity entity = new Entity();
        entity.addComponent(new Position(new Matrix4()));
        if (assetsManager.hasAsset(className, RenderSystem.RenderModel.class)) {
            RenderSystem.RenderModel renderModel = assetsManager.getAsset(className, RenderSystem.RenderModel.class);
            entity.addComponent(new Render(renderModel));
        }
        if (assetsManager.hasAsset(className, btRigidBody.btRigidBodyConstructionInfo.class)) {
            btRigidBody.btRigidBodyConstructionInfo rigidBodyConfig = assetsManager.getAsset(className, btRigidBody.btRigidBodyConstructionInfo.class);
            entity.addComponent(new RigidBody(rigidBodyConfig));
        }
        return entity;
    }

    public Entity addEntity(String name, Matrix4 transform, Entity entity) {
        entity.setName(name);
        entity.getComponent(Position.class).setLocalTransform(transform);
        entityManager.addEntity(entity);
        return entity;
    }
}
