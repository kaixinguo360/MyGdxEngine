package com.my.demo.builder;

import com.badlogic.gdx.graphics.VertexAttributes;
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

    public static final long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

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

    public static Entity addEntity(Scene scene, Entity entity) {
        return scene.getEntityManager().addEntity(entity);
    }

    public static Entity tmpEntity(Scene scene) {
        Entity entity = new Entity();
        entity.setName("tmp");
        return scene.getEntityManager().addEntity(entity);
    }

    public static <T> T getAsset(Scene scene, String id, Class<T> type) {
        return scene.getEngine().getAssetsManager().getAsset(id, type);
    }
}
