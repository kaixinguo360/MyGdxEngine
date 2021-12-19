package com.my.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.my.utils.world.Config;
import com.my.utils.world.com.Constraint;
import lombok.NoArgsConstructor;

public class Constraints {

    @NoArgsConstructor
    public static class Point2PointConstraint extends Constraint {

        @Config public Vector3 pivotInA;
        @Config public Vector3 pivotInB;

        public Point2PointConstraint(String bodyA, String bodyB, Vector3 pivotInA, Vector3 pivotInB) {
            super(bodyA, bodyB);
            this.pivotInA = pivotInA;
            this.pivotInB = pivotInB;
        }

        @Override
        public btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB) {
            return new btPoint2PointConstraint(bodyA, bodyB, pivotInA, pivotInB);
        }
    }

    @NoArgsConstructor
    public static class FixedConstraint extends Constraint {

        @Config public Matrix4 frameInA;
        @Config public Matrix4 frameInB;

        public FixedConstraint(String bodyA, String bodyB, Matrix4 frameInA, Matrix4 frameInB) {
            super(bodyA, bodyB);
            this.frameInA = frameInA;
            this.frameInB = frameInB;
        }

        @Override
        public btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB) {
            return new btFixedConstraint(bodyA, bodyB, frameInA, frameInB);
        }
    }

    @NoArgsConstructor
    public static class ConnectConstraint extends Constraint {

        @Config public float breakingImpulseThreshold;

        public ConnectConstraint(String bodyA, String bodyB, float breakingImpulseThreshold) {
            super(bodyA, bodyB);
            this.breakingImpulseThreshold = breakingImpulseThreshold;
        }

        @Override
        public btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB) {
            Matrix4 tmp1 = new Matrix4();
            Matrix4 tmp2 = new Matrix4();
            tmp1.set(bodyA.getWorldTransform());
            tmp2.set(bodyB.getWorldTransform());
            tmp1.inv().mul(tmp2);
            tmp2.idt();
            btTypedConstraint constraint = new btFixedConstraint(bodyA, bodyB, tmp1, tmp2);
            constraint.setBreakingImpulseThreshold(breakingImpulseThreshold);
            return constraint;
        }
    }

    @NoArgsConstructor
    public static class SliderConstraint extends Constraint {

        @Config public Matrix4 frameInA;
        @Config public Matrix4 frameInB;
        @Config public boolean useLinearReferenceFrameA;

        public SliderConstraint(String bodyA, String bodyB, Matrix4 frameInA, Matrix4 frameInB, boolean useLinearReferenceFrameA) {
            super(bodyA, bodyB);
            this.frameInA = frameInA;
            this.frameInB = frameInB;
            this.useLinearReferenceFrameA = useLinearReferenceFrameA;
        }

        @Override
        public btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB) {
            return (bodyA != bodyB) ?
                    new btSliderConstraint(bodyA, bodyB, frameInA, frameInB, useLinearReferenceFrameA) :
                    new btSliderConstraint(bodyA, frameInA, useLinearReferenceFrameA);
        }
    }

    @NoArgsConstructor
    public static class HingeConstraint extends Constraint {

        @Config public Matrix4 frameInA;
        @Config public Matrix4 frameInB;
        @Config public boolean useLinearReferenceFrameA;

        public HingeConstraint(String bodyA, String bodyB, Matrix4 frameInA, Matrix4 frameInB, boolean useLinearReferenceFrameA) {
            super(bodyA, bodyB);
            this.frameInA = frameInA;
            this.frameInB = frameInB;
            this.useLinearReferenceFrameA = useLinearReferenceFrameA;
        }

        @Override
        public btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB) {
            return (bodyA != bodyB) ?
                    new btHingeConstraint(bodyA, bodyB, frameInA, frameInB, useLinearReferenceFrameA) :
                    new btHingeConstraint(bodyA, frameInA, useLinearReferenceFrameA);
        }
    }

}
