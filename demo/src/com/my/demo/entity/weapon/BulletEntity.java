package com.my.demo.entity.weapon;

import com.badlogic.gdx.graphics.Color;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.EntityBuilder;
import com.my.world.enhanced.builder.EntityGenerator;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.enhanced.script.RemoveByPositionScript;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.rigidbody.CapsuleBody;
import com.my.world.module.render.light.GLTFPointLight;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;

public class BulletEntity extends EnhancedEntity {

    public static GLTFModel model;
    public static TemplateRigidBody body;
    public static EntityBuilder builder;

    public static void init(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();
        model = assetsManager.addAsset("bullet", GLTFModel.class, new GLTFModel("obj/bullet.gltf"));
        body = assetsManager.addAsset("bullet", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        builder = assetsManager.addAsset("bullet", EntityBuilder.class, (EntityGenerator) BulletEntity::new);
    }

    public final GLTFModelInstance render;
    public final PresetTemplateRigidBody rigidBody;
    public final Collision collision;
    public final RemoveByPositionScript removeByPositionScript;
    public final GLTFPointLight light;

    public BulletEntity() {
        setName("Bullet");
        render = addComponent(new GLTFModelInstance(model));
        rigidBody = addComponent(new PresetTemplateRigidBody(body));
        collision = addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        removeByPositionScript = addComponent(new RemoveByPositionScript());
        light = addComponent(new GLTFPointLight(Color.YELLOW.cpy(), 20f, 30f));
    }
}
