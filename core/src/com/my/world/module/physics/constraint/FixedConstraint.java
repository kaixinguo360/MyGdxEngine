package com.my.world.module.physics.constraint;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btFixedConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.module.common.Position;
import com.my.world.module.physics.Constraint;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FixedConstraint extends Constraint {

    @Config public Matrix4 frameInA;
    @Config public Matrix4 frameInB;

    public FixedConstraint(Entity base, Matrix4 frameInA, Matrix4 frameInB) {
        super(base);
        this.frameInA = frameInA;
        this.frameInB = frameInB;
    }

    @Override
    public btTypedConstraint get(btRigidBody base, btRigidBody self) {
        btFixedConstraint constraint = new btFixedConstraint(base, self, frameInA, frameInB);
        constraint.setBreakingImpulseThreshold(breakingImpulseThreshold);
        return constraint;
    }

    public static FixedConstraint newInstance(Entity base, Matrix4 tmp1, Matrix4 tmp2, float breakingImpulseThreshold) {
        tmp1.inv().mul(tmp2);
        tmp2.idt();
        FixedConstraint constraint = new FixedConstraint(base, tmp1, tmp2);
        constraint.breakingImpulseThreshold = breakingImpulseThreshold;
        return constraint;
    }

    public static FixedConstraint newInstance(Entity base, Entity attachTo, float breakingImpulseThreshold) {
        return newInstance(
                base,
                base.getComponent(Position.class).getGlobalTransform(new Matrix4()),
                attachTo.getComponent(Position.class).getGlobalTransform(new Matrix4()),
                breakingImpulseThreshold
        );
    }

    public static FixedConstraint connect(Entity base, Entity attachTo, float breakingImpulseThreshold) {
        FixedConstraint constraint = newInstance(
                base,
                base.getComponent(Position.class).getGlobalTransform(new Matrix4()),
                attachTo.getComponent(Position.class).getGlobalTransform(new Matrix4()),
                breakingImpulseThreshold
        );
        attachTo.addComponent(constraint);
        return constraint;
    }
}
