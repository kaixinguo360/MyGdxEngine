package com.my.demo.entity.object;

import com.badlogic.gdx.math.Vector3;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;

public class GroundEntity extends EnhancedEntity {

    public static GLTFModel model;
    public static TemplateRigidBody body;

    public static void init(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();
        model = assetsManager.addAsset("ground", GLTFModel.class, new GLTFModel("obj/ground.gltf"));
        body = assetsManager.addAsset("ground", TemplateRigidBody.class, new BoxBody(new Vector3(5000,0.005f,10000), 0f));
    }

    public final GLTFModelInstance render;
    public final PresetTemplateRigidBody rigidBody;

    public GroundEntity() {
        setName("ground");
        render = addComponent(new GLTFModelInstance(model));
        rigidBody = addComponent(new PresetTemplateRigidBody(body));
    }
}
