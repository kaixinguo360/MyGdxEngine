package com.my.demo.entity.weapon;

import com.badlogic.gdx.math.Vector3;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.EntityBuilder;
import com.my.world.enhanced.builder.EntityGenerator;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.enhanced.script.RemoveByPositionScript;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.CapsuleBody;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;

public class BombEntity extends EnhancedEntity {

    public static GLTFModel model;
    public static TemplateRigidBody body;
    public static EntityBuilder builder;

    public static void init(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();
        model = assetsManager.addAsset("bomb", GLTFModel.class, new GLTFModel("obj/bomb.gltf"));
        body = assetsManager.addAsset("bomb", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
        builder = assetsManager.addAsset("bomb", EntityBuilder.class, (EntityGenerator) BombEntity::new);
    }

    public final GLTFModelInstance render;
    public final PresetTemplateRigidBody rigidBody;
    public final RemoveByPositionScript removeByPositionScript;
    public final BombScript bombScript;
    public final DragForce dragForce1;
    public final DragForce dragForce2;

    public BombEntity() {
        setName("Bomb");
        render = addComponent(new GLTFModelInstance(model));
        rigidBody = addComponent(new PresetTemplateRigidBody(body));
        rigidBody.isEnableCallback = true;
        removeByPositionScript = addComponent(new RemoveByPositionScript());
        bombScript = addComponent(new BombScript());
        bombScript.explosionBuilder = ExplosionEntity.builder;
        dragForce1 = addComponent(new DragForce(new Vector3(0, 0, 0.05f), new Vector3(0, -1, 0), false));
        dragForce2 = addComponent(new DragForce(new Vector3(0.05f, 0, 0), new Vector3(0, -1, 0), false));
    }
}
