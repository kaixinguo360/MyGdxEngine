package com.my.demo.builder.weapon;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.my.demo.builder.PrefabBuilder;
import com.my.demo.builder.common.RemoveByPositionScript;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.rigidbody.CapsuleBody;
import com.my.world.module.render.light.GLTFPointLight;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;

public class BulletBuilder extends PrefabBuilder<BulletBuilder> {

    {
        prefabName = "Bullet";
    }

    public GLTFModel model;
    public TemplateRigidBody body;

    @Override
    protected void createAssets(Engine engine, Scene scene) {
        model = assetsManager.addAsset("bullet", GLTFModel.class, new GLTFModel("obj/bullet.gltf"));
        body = assetsManager.addAsset("bullet", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Bullet");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModelInstance(model));
        entity.addComponent(new PresetTemplateRigidBody(body));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveByPositionScript());
        entity.addComponent(new GLTFPointLight(Color.YELLOW.cpy(), 20f, 30f));
        scene.addEntity(entity);
    }
}
