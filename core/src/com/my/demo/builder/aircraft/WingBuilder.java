package com.my.demo.builder.aircraft;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.PrefabBuilder;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.GLTFModel;

public class WingBuilder extends PrefabBuilder<WingBuilder> {

    {
        prefabName = "Wing";
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Wing");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModel("obj/wing.gltf"));
        entity.addComponent(new BoxBody(new Vector3(1f,0.1f,0.5f), 25f));
        entity.addComponent(new ConnectConstraint(scene.tmpEntity(), 500));
        entity.addComponent(new DragForce(new Vector3(0, 30, 0), new Vector3(), false));
        scene.addEntity(entity);
    }
}
