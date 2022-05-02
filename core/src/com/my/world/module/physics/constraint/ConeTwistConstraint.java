package com.my.world.module.physics.constraint;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btConeTwistConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.module.physics.Constraint;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConeTwistConstraint extends Constraint {

    @Config public Matrix4 rbAFrame;
    @Config public Matrix4 rbBFrame;

    @Config public Float swingSpan1;
    @Config public Float swingSpan2; // No use, don't know why
    @Config public Float twistSpan;
    @Config public Float softness;
    @Config public Float biasFactor;

    public ConeTwistConstraint(Entity base, Matrix4 rbAFrame, Matrix4 rbBFrame) {
        super(base);
        this.rbAFrame = rbAFrame;
        this.rbBFrame = rbBFrame;
    }

    @Override
    public btTypedConstraint get(btRigidBody base, btRigidBody self) {
        btConeTwistConstraint constraint = new btConeTwistConstraint(base, self, rbAFrame, rbBFrame);
        constraint.setBreakingImpulseThreshold(breakingImpulseThreshold);
        if (swingSpan1 != null && swingSpan2 != null && twistSpan != null) {
            if (softness != null) {
                if (biasFactor != null) {
                    constraint.setLimit((float) Math.toRadians(swingSpan1), (float) Math.toRadians(swingSpan2), (float) Math.toRadians(twistSpan), softness, biasFactor);
                } else {
                    constraint.setLimit((float) Math.toRadians(swingSpan1), (float) Math.toRadians(swingSpan2), (float) Math.toRadians(twistSpan), softness);
                }
            } else {
                constraint.setLimit((float) Math.toRadians(swingSpan1), (float) Math.toRadians(swingSpan2), (float) Math.toRadians(twistSpan));
            }
        }
        return constraint;
    }
}
