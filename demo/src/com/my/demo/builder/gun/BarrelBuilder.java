package com.my.demo.builder.gun;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.PrefabBuilder;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.GLTFModel;

public class BarrelBuilder extends PrefabBuilder<BarrelBuilder> {

    {
        prefabName = "Barrel";
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Barrel");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModel("obj/body.gltf"));
        entity.addComponent(new BoxBody(new Vector3(0.5f,0.5f,2.5f), 5f));
        entity.addComponent(new ConnectConstraint(scene.tmpEntity(), 2000));
        scene.addEntity(entity);
    }
}
