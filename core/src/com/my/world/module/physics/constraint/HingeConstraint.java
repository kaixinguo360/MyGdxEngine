package com.my.world.module.physics.constraint;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btHingeConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.module.physics.Constraint;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class HingeConstraint extends Constraint {

    @Config
    public Matrix4 frameInA;
    @Config
    public Matrix4 frameInB;
    @Config
    public boolean useLinearReferenceFrameA;

    public HingeConstraint(Entity base, Matrix4 frameInA, Matrix4 frameInB, boolean useLinearReferenceFrameA) {
        super(base);
        this.frameInA = frameInA;
        this.frameInB = frameInB;
        this.useLinearReferenceFrameA = useLinearReferenceFrameA;
    }

    @Override
    public btTypedConstraint get(btRigidBody base, btRigidBody self) {
        return (base != self) ?
                new btHingeConstraint(base, self, frameInA, frameInB, useLinearReferenceFrameA) :
                new btHingeConstraint(base, frameInA, useLinearReferenceFrameA);
    }
}
