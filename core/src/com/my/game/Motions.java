package com.my.game;

import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Config;
import com.my.utils.world.com.Motion;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class Motions {

    private static final Vector3 TMP_1 = new Vector3();
    private static final Vector3 TMP_2 = new Vector3();

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Force extends Motion {

        @Config
        public Vector3 force;

        @Config
        public Vector3 rel_pos;

        @Override
        public void update() {
            rigidBody.body.applyForce(TMP_1.set(force).rot(position.transform), rel_pos);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class FixedForce extends Motion {

        @Config
        public Vector3 force;

        @Config
        public Vector3 rel_pos;

        @Override
        public void update() {
            rigidBody.body.applyForce(force, rel_pos);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class LimitedForce extends Motion {

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

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Lift extends Motion {

        @Config
        public Vector3 up;

        @Override
        public void update() {
            TMP_1.set(rigidBody.body.getLinearVelocity());
            TMP_2.set(up).rot(position.transform);
            float lift = - TMP_2.dot(TMP_1);
            TMP_1.set(up).nor().scl(lift).rot(position.transform);
            TMP_2.set(0, 0, 0);
            rigidBody.body.applyForce(TMP_1, TMP_2);
        }
    }

}
