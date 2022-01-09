package com.my.world.module.physics.force;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class DragForce extends Force {

    private static final Vector3 TMP_1 = new Vector3();
    private static final Vector3 TMP_2 = new Vector3();

    @Config
    public Vector3 direction;

    @Config
    public Vector3 rel_pos;

    @Config
    public boolean global;

    @Override
    public void update() {
        Matrix4 transform = position.getLocalTransform();
        Vector3 direction = TMP_2.set(this.direction).nor(); if (!global) direction.rot(transform);
        Vector3 velocity = TMP_1.set(rigidBody.body.getLinearVelocity());
        float coefficient = -direction.dot(velocity);
        Vector3 force = TMP_1.set(direction).scl(coefficient * Math.abs(coefficient) * this.direction.len());
        Vector3 rel_pos = TMP_2.set(this.rel_pos).rot(transform);
        rigidBody.body.applyForce(force, rel_pos);
    }
}
