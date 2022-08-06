package com.my.demo.entity.object;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;

public class WallEntity extends EnhancedEntity {

    public static final int height = 5;
    public static GLTFModel model;
    public static TemplateRigidBody body;

    public static void init(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();
        model = assetsManager.addAsset("brick", GLTFModel.class, new GLTFModel("obj/brick.gltf"));
        body = assetsManager.addAsset("brick", TemplateRigidBody.class, new BoxBody(new Vector3(1,0.5f,0.5f), 50f));
    }

    public WallEntity() {
        setName("Wall");
        Matrix4 tmpM = Matrix4Pool.obtain();
        for (int i = 0; i < height; i++) {
            float tmp = 0.5f + (i % 2);
            for (int j = 0; j < 10; j+=2) {
                EnhancedEntity entity1 = new EnhancedEntity();
                entity1.setName("Brick");
                entity1.setParent(this);
                entity1.transform.setToTranslation(tmp + j, 0.5f + i, 0);
                entity1.decompose();
                entity1.addComponent(new GLTFModelInstance(model));
                entity1.addComponent(new PresetTemplateRigidBody(body));
                addEntity(entity1);
            }
        }
        Matrix4Pool.free(tmpM);
    }
}
