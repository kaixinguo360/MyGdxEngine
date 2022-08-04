package com.my.demo.builder.object;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.PrefabBuilder;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;

public class WallBuilder extends PrefabBuilder<WallBuilder> {

    {
        prefabName = "Wall";
    }

    public static final int height = 5;
    public GLTFModel model;
    public TemplateRigidBody body;

    @Override
    protected void createAssets(Engine engine, Scene scene) {
        model = assetsManager.addAsset("brick", GLTFModel.class, new GLTFModel("obj/brick.gltf"));
        body = assetsManager.addAsset("brick", TemplateRigidBody.class, new BoxBody(new Vector3(1,0.5f,0.5f), 50f));
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Wall");
        entity.addComponent(new Position(new Matrix4()));
        scene.addEntity(entity);
        Matrix4 tmpM = Matrix4Pool.obtain();
        for (int i = 0; i < height; i++) {
            float tmp = 0.5f + (i % 2);
            for (int j = 0; j < 10; j+=2) {
                Entity entity1 = new Entity();
                entity1.setName("Brick");
                entity1.setParent(entity);
                entity1.addComponent(new Position(new Matrix4().setToTranslation(tmp + j, 0.5f + i, 0)));
                entity1.addComponent(new GLTFModelInstance(model));
                entity1.addComponent(new PresetTemplateRigidBody(body));
                scene.addEntity(entity1);
            }
        }
        Matrix4Pool.free(tmpM);
    }
}
