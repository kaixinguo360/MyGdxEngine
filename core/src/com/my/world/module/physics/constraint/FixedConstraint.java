package com.my.world.module.physics.constraint;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btFixedConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Config;
import com.my.world.core.Entity;
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
}
