package com.my.utils.world.com;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.utils.world.Component;

public abstract class Motion implements Component {
    public abstract void update(btRigidBody body, Position position);

    public static class Force extends Motion {
        private static final Vector3 tmp = new Vector3();
        private final Vector3 force;
        private final Vector3 rel_pos;
        public Force(Vector3 force) {
            this(force, new Vector3());
        }
        public Force(Vector3 force, Vector3 rel_pos) {
            this.force = force;
            this.rel_pos = rel_pos;
        }
        @Override
        public void update(btRigidBody body, Position position) {
            body.applyForce(tmp.set(force).rot(position.transform), rel_pos);
        }
    }
    public static class FixedForce extends Motion {
        private final Vector3 force;
        private final Vector3 rel_pos;
        public FixedForce(Vector3 force) {
            this(force, new Vector3());
        }
        public FixedForce(Vector3 force, Vector3 rel_pos) {
            this.force = force;
            this.rel_pos = rel_pos;
        }
        @Override
        public void update(btRigidBody body, Position position) {
            body.applyForce(force, rel_pos);
        }
    }
    public static class LimitedForce extends Motion {
        private static final Vector3 tmp = new Vector3();
        private final float maxVelocity;
        private final Vector3 force;
        private final Vector3 rel_pos;
        public LimitedForce(float maxVelocity, Vector3 force) {
            this(maxVelocity, force, new Vector3());
        }
        public LimitedForce(float maxVelocity, Vector3 force, Vector3 rel_pos) {
            this.maxVelocity = maxVelocity;
            this.force = force;
            this.rel_pos = rel_pos;
        }
        @Override
        public void update(btRigidBody body, Position position) {
            tmp.set(force).rot(position.transform).nor();
            if (Math.abs(body.getLinearVelocity().dot(tmp)) <= maxVelocity) {
                body.applyForce(tmp.set(force).rot(position.transform), rel_pos);
            }
        }
    }
    public static class Lift extends Motion {
        private static final Vector3 tmp1 = new Vector3();
        private static final Vector3 tmp2 = new Vector3();
        private Vector3 up;
        public Lift(Vector3 up) {
            this.up = up;
        }
        @Override
        public void update(btRigidBody body, Position position) {
            tmp1.set(body.getLinearVelocity());
            tmp2.set(up).rot(position.transform);
            float lift = - tmp2.dot(tmp1);
            tmp1.set(up).nor().scl(lift).rot(position.transform);
            tmp2.set(0, 0, 0);
            body.applyForce(tmp1, tmp2);
        }
    }
}
