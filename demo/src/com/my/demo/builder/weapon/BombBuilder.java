package com.my.demo.builder.weapon;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.PrefabBuilder;
import com.my.demo.builder.common.RemoveByPositionScript;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Prefab;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.Collision;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.CapsuleBody;
import com.my.world.module.render.model.GLTFModel;
import com.my.world.module.render.model.GLTFModelInstance;

public class BombBuilder extends PrefabBuilder<BombBuilder> {

    {
        prefabName = "Bomb";
    }

    public GLTFModel model;
    public TemplateRigidBody body;
    public Prefab bombPrefab;

    @Override
    protected void initDependencies() {
        bombPrefab = getDependency(ExplosionBuilder.class).prefab;
    }

    @Override
    protected void createAssets(Engine engine, Scene scene) {
        model = assetsManager.addAsset("bomb", GLTFModel.class, new GLTFModel("obj/bomb.gltf"));
        body = assetsManager.addAsset("bomb", TemplateRigidBody.class, new CapsuleBody(0.5f, 1, 50f));
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Bomb");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModelInstance(model));
        entity.addComponent(new PresetTemplateRigidBody(body));
        entity.addComponent(new Collision(Collision.NORMAL_FLAG, Collision.ALL_FLAG));
        entity.addComponent(new RemoveByPositionScript());
        entity.addComponent(new BombScript()).explosionPrefab = bombPrefab;
        entity.addComponent(new DragForce(new Vector3(0, 0, 0.05f), new Vector3(0, -1, 0), false));
        entity.addComponent(new DragForce(new Vector3(0.05f, 0, 0), new Vector3(0, -1, 0), false));
        scene.addEntity(entity);
    }
}
