package com.my.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.World;
import com.my.utils.world.com.Constraint;
import com.my.utils.world.sys.ConstraintSystem;

import java.util.HashMap;
import java.util.Map;

public class Constraints {

    public static void init(World world) {
        AssetsManager assetsManager = world.getAssetsManager();
        assetsManager.addAsset("Point2PointConstraint", ConstraintSystem.ConstraintType.class, new Point2PointConstraint());
        assetsManager.addAsset("FixedConstraint", ConstraintSystem.ConstraintType.class, new FixedConstraint());
        assetsManager.addAsset("ConnectConstraint", ConstraintSystem.ConstraintType.class, new ConnectConstraint());
        assetsManager.addAsset("SliderConstraint", ConstraintSystem.ConstraintType.class, new SliderConstraint());
        assetsManager.addAsset("HingeConstraint", ConstraintSystem.ConstraintType.class, new HingeConstraint());
    }

    public static class Point2PointConstraint implements ConstraintSystem.ConstraintType {
        @Override
        public btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB, Map<String, Object> config) {
            Vector3 pivotInA = (Vector3) config.get("pivotInA");
            Vector3 pivotInB = (Vector3) config.get("pivotInB");
            return new btPoint2PointConstraint(bodyA, bodyB, pivotInA, pivotInB);
        }
        public static Constraint getConfig(AssetsManager assetsManager, String bodyA, String bodyB, ConstraintSystem.ConstraintController controller, Vector3 pivotInA, Vector3 pivotInB) {
            return new Constraint(
                    bodyA, bodyB,
                    assetsManager.getAsset("Point2PointConstraint", ConstraintSystem.ConstraintType.class),
                    new HashMap<String, Object>() {{
                        put("pivotInA", pivotInA);
                        put("pivotInB", pivotInB);
                    }}, controller);
        }
    }
    public static class FixedConstraint implements ConstraintSystem.ConstraintType {
        @Override
        public btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB, Map<String, Object> config) {
            Matrix4 frameInA = (Matrix4) config.get("frameInA");
            Matrix4 frameInB = (Matrix4) config.get("frameInB");
            return new btFixedConstraint(bodyA, bodyB, frameInA, frameInB);
        }
        public static Constraint getConfig(AssetsManager assetsManager, String bodyA, String bodyB, ConstraintSystem.ConstraintController controller, Matrix4 frameInA, Matrix4 frameInB) {
            return new Constraint(
                    bodyA, bodyB,
                    assetsManager.getAsset("FixedConstraint", ConstraintSystem.ConstraintType.class),
                    new HashMap<String, Object>() {{
                        put("frameInA", frameInA);
                        put("frameInB", frameInB);
                    }}, controller);
        }
    }
    public static class ConnectConstraint implements ConstraintSystem.ConstraintType {
        @Override
        public btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB, Map<String, Object> config) {
            Matrix4 tmp1 = new Matrix4();
            Matrix4 tmp2 = new Matrix4();
            float breakingImpulseThreshold = (float) (double) config.get("breakingImpulseThreshold");
            tmp1.set(bodyA.getWorldTransform());
            tmp2.set(bodyB.getWorldTransform());
            tmp1.inv().mul(tmp2);
            tmp2.idt();
            btTypedConstraint constraint = new btFixedConstraint(bodyA, bodyB, tmp1, tmp2);
            constraint.setBreakingImpulseThreshold(breakingImpulseThreshold);
            return constraint;
        }
        public static Constraint getConfig(AssetsManager assetsManager, String bodyA, String bodyB, ConstraintSystem.ConstraintController controller, float breakingImpulseThreshold) {
            return new Constraint(
                    bodyA, bodyB,
                    assetsManager.getAsset("ConnectConstraint", ConstraintSystem.ConstraintType.class),
                    new HashMap<String, Object>() {{
                        put("breakingImpulseThreshold", (double) breakingImpulseThreshold);
                    }}, controller);
        }
    }
    public static class SliderConstraint implements ConstraintSystem.ConstraintType {
        @Override
        public btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB, Map<String, Object> config) {
            Matrix4 frameInA = (Matrix4) config.get("frameInA");
            Matrix4 frameInB = (Matrix4) config.get("frameInB");
            boolean useLinearReferenceFrameA = (Boolean) config.get("useLinearReferenceFrameA");
            return (bodyA != bodyB) ?
                    new btSliderConstraint(bodyA, bodyB, frameInA, frameInB, useLinearReferenceFrameA) :
                    new btSliderConstraint(bodyA, frameInA, useLinearReferenceFrameA);
        }
        public static Constraint getConfig(AssetsManager assetsManager, String bodyA, String bodyB, ConstraintSystem.ConstraintController controller, Matrix4 frameInA, Matrix4 frameInB, boolean useLinearReferenceFrameA) {
            return new Constraint(
                    bodyA, bodyB,
                    assetsManager.getAsset("SliderConstraint", ConstraintSystem.ConstraintType.class),
                    new HashMap<String, Object>() {{
                        put("frameInA", frameInA);
                        put("frameInB", frameInB);
                        put("useLinearReferenceFrameA", useLinearReferenceFrameA);
                    }}, controller);
        }
    }
    public static class HingeConstraint implements ConstraintSystem.ConstraintType {
        @Override
        public btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB, Map<String, Object> config) {
            Matrix4 frameInA = (Matrix4) config.get("frameInA");
            Matrix4 frameInB = (Matrix4) config.get("frameInB");
            boolean useLinearReferenceFrameA = (Boolean) config.get("useLinearReferenceFrameA");
            return (bodyA != bodyB) ?
                    new btHingeConstraint(bodyA, bodyB, frameInA, frameInB, useLinearReferenceFrameA) :
                    new btHingeConstraint(bodyA, frameInA, useLinearReferenceFrameA);
        }
        public static Constraint getConfig(AssetsManager assetsManager, String bodyA, String bodyB, ConstraintSystem.ConstraintController controller, Matrix4 frameInA, Matrix4 frameInB, boolean useLinearReferenceFrameA) {
            return new Constraint(
                    bodyA, bodyB,
                    assetsManager.getAsset("HingeConstraint", ConstraintSystem.ConstraintType.class),
                    new HashMap<String, Object>() {{
                        put("frameInA", frameInA);
                        put("frameInB", frameInB);
                        put("useLinearReferenceFrameA", useLinearReferenceFrameA);
                    }}, controller);
        }
    }

    public static class Loader implements com.my.utils.world.Loader {

        @Override
        public <E, T> T load(E config, Class<T> type) {
            try {
                return (T) Class.forName(((Map<String, String>) config).get("type")).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException("MotionHandler create error: " + e.getMessage(), e);
            }
        }

        @Override
        public <E, T> E getConfig(T obj, Class<E> configType) {
            return (E) new HashMap<String, String>() {{
                put("type", obj.getClass().getName());
            }};
        }

        @Override
        public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
            return (Map.class.isAssignableFrom(configType)) && (ConstraintSystem.ConstraintType.class.isAssignableFrom(targetType));
        }
    }
}
