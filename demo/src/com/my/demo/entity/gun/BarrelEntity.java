package com.my.demo.entity.gun;

import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Entity;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.model.GLTFModel;

public class BarrelEntity extends EnhancedEntity {

    public final GLTFModel render;
    public final BoxBody rigidBody;
    public final ConnectConstraint constraint;

    public BarrelEntity() {
        this(null);
    }

    public BarrelEntity(Entity baseEntity) {
        setName("Barrel");
        render = addComponent(new GLTFModel("obj/body.gltf"));
        rigidBody = addComponent(new BoxBody(new Vector3(0.5f, 0.5f, 2.5f), 5f));
        constraint = baseEntity == null ? null : addComponent(new ConnectConstraint(baseEntity, 2000));
    }
}
