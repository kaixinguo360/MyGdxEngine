package com.my.world.enhanced.physics;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Entity;
import com.my.world.module.common.Position;
import com.my.world.module.physics.Constraint;
import com.my.world.module.physics.constraint.*;

public class ConstraintUtil {

    // FixedConstraint //

    public static ConstraintBuilder<FixedConstraint> fixedConstraintBuilder = FixedConstraint::new;
    public static FixedConstraint connect(Entity entityA, Entity entityB, float breakingImpulseThreshold) {
        return constraint(fixedConstraintBuilder, entityA, entityB, breakingImpulseThreshold);
    }
    public static FixedConstraint connect(Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA) {
        return constraint(fixedConstraintBuilder, entityA, entityB, breakingImpulseThreshold, frameInA);
    }
    public static FixedConstraint connect(Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA, Matrix4 frameInB) {
        return constraint(fixedConstraintBuilder, entityA, entityB, breakingImpulseThreshold, frameInA, frameInB);
    }

    // HingeConstraint //

    public static ConstraintBuilder<HingeConstraint> hingeConstraintBuilder = (base, fa, fb) -> new HingeConstraint(base, fa, fb, false);
    public static HingeConstraint hinge(Entity entityA, Entity entityB, float breakingImpulseThreshold) {
        return constraint(hingeConstraintBuilder, entityA, entityB, breakingImpulseThreshold);
    }
    public static HingeConstraint hinge(Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA) {
        return constraint(hingeConstraintBuilder, entityA, entityB, breakingImpulseThreshold, frameInA);
    }
    public static HingeConstraint hinge(Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA, Matrix4 frameInB) {
        return constraint(hingeConstraintBuilder, entityA, entityB, breakingImpulseThreshold, frameInA, frameInB);
    }

    // SliderConstraint //

    public static ConstraintBuilder<SliderConstraint> sliderConstraintBuilder = (base, fa, fb) -> new SliderConstraint(base, fa, fb, false);
    public static SliderConstraint slider(Entity entityA, Entity entityB, float breakingImpulseThreshold) {
        return constraint(sliderConstraintBuilder, entityA, entityB, breakingImpulseThreshold);
    }
    public static SliderConstraint slider(Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA) {
        return constraint(sliderConstraintBuilder, entityA, entityB, breakingImpulseThreshold, frameInA);
    }
    public static SliderConstraint slider(Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA, Matrix4 frameInB) {
        return constraint(sliderConstraintBuilder, entityA, entityB, breakingImpulseThreshold, frameInA, frameInB);
    }

    // ConeTwistConstraint //

    public static ConstraintBuilder<ConeTwistConstraint> coneTwistConstraintBuilder = ConeTwistConstraint::new;
    public static ConeTwistConstraint coneTwist(Entity entityA, Entity entityB, float breakingImpulseThreshold) {
        return constraint(coneTwistConstraintBuilder, entityA, entityB, breakingImpulseThreshold);
    }
    public static ConeTwistConstraint coneTwist(Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA) {
        return constraint(coneTwistConstraintBuilder, entityA, entityB, breakingImpulseThreshold, frameInA);
    }
    public static ConeTwistConstraint coneTwist(Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA, Matrix4 frameInB) {
        return constraint(coneTwistConstraintBuilder, entityA, entityB, breakingImpulseThreshold, frameInA, frameInB);
    }

    // Point2PointConstraint //

    public static ConstraintBuilder<Point2PointConstraint> point2PointConstraintBuilder = (base, fa, fb) -> new Point2PointConstraint(base, new Vector3().mul(fa), new Vector3().mul(fb));
    public static Point2PointConstraint point2Point(Entity entityA, Entity entityB, float breakingImpulseThreshold) {
        return constraint(point2PointConstraintBuilder, entityA, entityB, breakingImpulseThreshold);
    }
    public static Point2PointConstraint point2Point(Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA) {
        return constraint(point2PointConstraintBuilder, entityA, entityB, breakingImpulseThreshold, frameInA);
    }
    public static Point2PointConstraint point2Point(Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA, Matrix4 frameInB) {
        return constraint(point2PointConstraintBuilder, entityA, entityB, breakingImpulseThreshold, frameInA, frameInB);
    }

    // Constraint A to B //

    public static <T extends Constraint> T constraint(ConstraintBuilder<T> builder, Entity entityA, Entity entityB, float breakingImpulseThreshold) {
        return constraint(builder, entityA, entityB, breakingImpulseThreshold, new Matrix4());
    }
    public static <T extends Constraint> T constraint(ConstraintBuilder<T> builder, Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA) {
        Position positionA = entityA.getComponent(Position.class);
        if (positionA == null) throw new RuntimeException("No position component in this entity: id=" + entityA.getId());
        Position positionB = entityB.getComponent(Position.class);
        if (positionB == null) throw new RuntimeException("No position component in this entity: id=" + entityB.getId());
        Matrix4 frameInB = positionB.getGlobalTransform(new Matrix4());
        frameInB.inv().mul(positionA.getGlobalTransform()).mul(frameInA);
        return constraint(builder, entityA, entityB, breakingImpulseThreshold, frameInA, frameInB);
    }
    public static <T extends Constraint> T constraint(ConstraintBuilder<T> builder, Entity entityA, Entity entityB, float breakingImpulseThreshold, Matrix4 frameInA, Matrix4 frameInB) {
        T constraint = builder.build(entityB, frameInB, frameInA);
        constraint.breakingImpulseThreshold = breakingImpulseThreshold;
        entityA.addComponent(constraint);
        return constraint;
    }

    public interface ConstraintBuilder<T extends Constraint> {
        T build(Entity base, Matrix4 frameInA, Matrix4 frameInB);
    }
}
