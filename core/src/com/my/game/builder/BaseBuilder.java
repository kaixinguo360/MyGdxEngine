package com.my.game.builder;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.AssetsManager;
import com.my.world.core.Entity;
import com.my.world.core.EntityManager;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.RigidBodyConfig;
import com.my.world.module.render.Render;
import com.my.world.module.render.RenderModel;

public class BaseBuilder {

    protected final AssetsManager assetsManager;
    protected final EntityManager entityManager;

    public BaseBuilder(Scene scene) {
        this.assetsManager = scene.getEngine().getAssetsManager();
        this.entityManager = scene.getEntityManager();
    }

    public BaseBuilder(AssetsManager assetsManager, EntityManager entityManager) {
        this.assetsManager = assetsManager;
        this.entityManager = entityManager;
    }

    public Entity createEntity(String className) {
        Entity entity = new Entity();
        entity.addComponent(new Position(new Matrix4()));
        if (assetsManager.hasAsset(className, RenderModel.class)) {
            RenderModel renderModel = assetsManager.getAsset(className, RenderModel.class);
            entity.addComponent(new Render(renderModel));
        }
        if (assetsManager.hasAsset(className, RigidBodyConfig.class)) {
            RigidBodyConfig rigidBodyConfig = assetsManager.getAsset(className, RigidBodyConfig.class);
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
