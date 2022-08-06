package com.my.demo.entity.object;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Entity;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.physics.rigidbody.CylinderBody;
import com.my.world.module.physics.script.ConstraintController;
import com.my.world.module.render.model.GLTFModel;

public class RotateEntity<E extends ConstraintController> extends EnhancedEntity {

    public final GLTFModel render;
    public final CylinderBody rigidBody;
    public final HingeConstraint constraint;
    public final E controller;

    public RotateEntity() {
        this(null, null);
    }

    public RotateEntity(Entity baseEntity, E constraintController) {
        setName("Rotate");
        render = addComponent(new GLTFModel("obj/rotate.gltf"));
        rigidBody = addComponent(new CylinderBody(new Vector3(0.5f, 0.5f, 0.5f), 50f));
        constraint = baseEntity == null ? null : addComponent(
                new HingeConstraint(
                        baseEntity,
                        new Matrix4().rotate(Vector3.X, 90),
                        new Matrix4().rotate(Vector3.X, 90),
                        false
                )
        );
        controller = constraintController == null ? null : addComponent(constraintController);
    }
}
