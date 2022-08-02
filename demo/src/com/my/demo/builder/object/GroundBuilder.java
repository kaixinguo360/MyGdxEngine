package com.my.demo.builder.object;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.PrefabBuilder;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;

public class GroundBuilder extends PrefabBuilder<GroundBuilder> {

    {
        prefabName = "Ground";
    }

    public GLTFModel model;
    public TemplateRigidBody body;

    @Override
    protected void createAssets(Engine engine, Scene scene) {
        model = assetsManager.addAsset("ground", GLTFModel.class, new GLTFModel("obj/ground.gltf"));
        body = assetsManager.addAsset("ground", TemplateRigidBody.class, new BoxBody(new Vector3(5000,0.005f,10000), 0f));
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity ground = new Entity();
        ground.setName("ground");
        ground.addComponent(new Position(new Matrix4()));
        ground.addComponent(new GLTFModelInstance(model));
        ground.addComponent(new PresetTemplateRigidBody(body));
        scene.addEntity(ground);
    }
}
