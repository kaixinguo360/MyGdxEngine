package com.my.demo.entity.aircraft;

import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.GLTFModel;

public class WingEntity extends EnhancedEntity {

    public final GLTFModel render;
    public final BoxBody rigidBody;
    public final DragForce dragForce;

    public WingEntity() {
        setName("Wing");
        render = addComponent(new GLTFModel("obj/wing.gltf"));
        rigidBody = addComponent(new BoxBody(new Vector3(1f, 0.1f, 0.5f), 25f));
        dragForce = addComponent(new DragForce(new Vector3(0, 30, 0), new Vector3(), false));
    }
}
