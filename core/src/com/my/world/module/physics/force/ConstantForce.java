package com.my.world.module.physics.force;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ConstantForce extends Force {

    private static final Vector3 TMP_1 = new Vector3();
    private static final Vector3 TMP_2 = new Vector3();

    @Config
    public Vector3 force;

    @Config
    public Vector3 rel_pos;

    @Config
    public boolean global;

    @Override
    public void update() {
        Matrix4 transform = position.getLocalTransform();
        Vector3 force = TMP_1.set(this.force); if (!global) force.rot(transform);
        Vector3 rel_pos = TMP_2.set(this.rel_pos).rot(transform);
        rigidBody.body.applyForce(force, rel_pos);
    }
}
