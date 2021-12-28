package com.my.world.module.physics.motion;

import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Lift extends Motion {

    private static final Vector3 TMP_1 = new Vector3();
    private static final Vector3 TMP_2 = new Vector3();

    @Config
    public Vector3 up;

    @Override
    public void update() {
        TMP_1.set(rigidBody.body.getLinearVelocity());
        TMP_2.set(up).rot(position.getLocalTransform());
        float lift = -TMP_2.dot(TMP_1);
        TMP_1.set(up).nor().scl(lift).rot(position.getLocalTransform());
        TMP_2.set(0, 0, 0);
        rigidBody.body.applyForce(TMP_1, TMP_2);
    }
}
