package com.my.demo.builder.aircraft;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.PrefabBuilder;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.force.ConstantForce;
import com.my.world.module.physics.rigidbody.ConeBody;
import com.my.world.module.render.model.GLTFModel;

public class EngineBuilder extends PrefabBuilder<EngineBuilder> {

    {
        prefabName = "Engine";
    }

    public static final float force = 4000;

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Engine");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModel("obj/engine.gltf"));
        entity.addComponent(new ConeBody(0.45f,1, 50));
        entity.addComponent(new ConnectConstraint(scene.tmpEntity(), 2000));
        entity.addComponent(new ConstantForce(new Vector3(0, force, 0), new Vector3(), false));
        scene.addEntity(entity);
    }
}
