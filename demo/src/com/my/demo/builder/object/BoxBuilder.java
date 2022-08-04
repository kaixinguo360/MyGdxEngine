package com.my.demo.builder.object;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.PrefabBuilder;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;

public class BoxBuilder extends PrefabBuilder<BoxBuilder> {

    {
        prefabName = "Box";
    }

    public GLTFModel model;
    public TemplateRigidBody body;

    @Override
    protected void createAssets(Engine engine, Scene scene) {
        model = assetsManager.addAsset("box", GLTFModel.class, new GLTFModel("obj/box.gltf"));
        body = assetsManager.addAsset("box", TemplateRigidBody.class, new BoxBody(new Vector3(0.5f,0.5f,0.5f), 50f));
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Box");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModelInstance(model));
        entity.addComponent(new PresetTemplateRigidBody(body));
        scene.addEntity(entity);
    }
}
