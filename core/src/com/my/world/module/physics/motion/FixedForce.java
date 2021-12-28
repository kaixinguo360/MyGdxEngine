package com.my.world.module.physics.motion;

import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class FixedForce extends Motion {

    @Config
    public Vector3 force;

    @Config
    public Vector3 rel_pos;

    @Override
    public void update() {
        rigidBody.body.applyForce(force, rel_pos);
    }
}
