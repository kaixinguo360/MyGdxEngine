package com.my.demo.builder;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.AssetsManager;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.PresetModelRender;

public class BaseBuilder {

    public static Entity createEntity(Scene scene, String className) {
        Entity entity = new Entity();
        AssetsManager assetsManager = scene.getEngine().getAssetsManager();
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

    public static Entity addEntity(Scene scene, String name, Matrix4 transform, Entity entity) {
        entity.setName(name);
        entity.getComponent(Position.class).setLocalTransform(transform);
        scene.getEntityManager().addEntity(entity);
        return entity;
    }

    public static Entity addEntity(Scene scene, Entity entity) {
        return scene.getEntityManager().addEntity(entity);
    }

    public static <T> T getAsset(Scene scene, String id, Class<T> type) {
        return scene.getEngine().getAssetsManager().getAsset(id, type);
    }
}
