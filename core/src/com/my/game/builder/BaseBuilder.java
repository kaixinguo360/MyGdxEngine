package com.my.game.builder;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.AssetsManager;
import com.my.world.core.Entity;
import com.my.world.core.EntityManager;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.PresetModelRender;

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
        if (assetsManager.hasAsset(className, ModelRender.class)) {
            ModelRender modelRender = assetsManager.getAsset(className, ModelRender.class);
            entity.addComponent(new PresetModelRender(modelRender));
        }
        if (assetsManager.hasAsset(className, TemplateRigidBody.class)) {
            TemplateRigidBody templateRigidBody = assetsManager.getAsset(className, TemplateRigidBody.class);
            entity.addComponent(new PresetTemplateRigidBody(templateRigidBody));
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
