package com.my.game.script.motion;

import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Config;
import com.my.utils.world.com.Motion;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class LimitedForce extends Motion {

    private static final Vector3 TMP_1 = new Vector3();

    @Config
    public float maxVelocity;

    @Config
    public Vector3 force;

    @Config
    public Vector3 rel_pos;

    @Override
    public void update() {
        TMP_1.set(force).rot(position.transform).nor();
        if (Math.abs(rigidBody.body.getLinearVelocity().dot(TMP_1)) <= maxVelocity) {
            rigidBody.body.applyForce(TMP_1.set(force).rot(position.transform), rel_pos);
        }
    }
}
