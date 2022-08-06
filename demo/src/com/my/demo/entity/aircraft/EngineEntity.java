package com.my.demo.entity.aircraft;

import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Entity;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.force.ConstantForce;
import com.my.world.module.physics.rigidbody.ConeBody;
import com.my.world.module.render.model.GLTFModel;

public class EngineEntity extends EnhancedEntity {

    public static final float force = 4000;

    public final GLTFModel render;
    public final ConeBody rigidBody;
    public final ConnectConstraint constraint;
    public final ConstantForce constantForce;

    public EngineEntity() {
        this(null);
    }

    public EngineEntity(Entity baseEntity) {
        setName("Engine");
        render = addComponent(new GLTFModel("obj/engine.gltf"));
        rigidBody = addComponent(new ConeBody(0.45f,1, 50));
        constraint = baseEntity == null ? null : addComponent(new ConnectConstraint(baseEntity, 2000));
        constantForce = addComponent(new ConstantForce(new Vector3(0, force, 0), new Vector3(), false));
    }
}
