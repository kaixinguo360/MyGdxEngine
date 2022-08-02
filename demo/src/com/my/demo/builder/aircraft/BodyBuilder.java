package com.my.demo.builder.aircraft;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.builder.PrefabBuilder;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.GLTFModel;

public class BodyBuilder extends PrefabBuilder<BodyBuilder> {

    {
        prefabName = "Body";
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Body");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModel("obj/body.gltf"));
        entity.addComponent(new BoxBody(new Vector3(0.5f,0.5f,2.5f), 50f));
        entity.addComponent(new DragForce(new Vector3(0, 0, 1.2f), new Vector3(), false));
        scene.addEntity(entity);
    }
}
