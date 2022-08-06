package com.my.demo.entity.aircraft;

import com.badlogic.gdx.math.Vector3;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.force.DragForce;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.GLTFModel;

public class BodyEntity extends EnhancedEntity {

    public final GLTFModel render;
    public final BoxBody rigidBody;
    public final DragForce dragForce;

    public BodyEntity() {
        setName("Body");
        render = addComponent(new GLTFModel("obj/body.gltf"));
        rigidBody = addComponent(new BoxBody(new Vector3(0.5f,0.5f,2.5f), 50f));
        dragForce = addComponent(new DragForce(new Vector3(0, 0, 1.2f), new Vector3(), false));
    }
}
