package com.my.game.constraint;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSliderConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.utils.world.Config;
import com.my.utils.world.Entity;
import com.my.utils.world.com.Constraint;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SliderConstraint extends Constraint {

    @Config
    public Matrix4 frameInA;
    @Config
    public Matrix4 frameInB;
    @Config
    public boolean useLinearReferenceFrameA;

    public SliderConstraint(Entity base, Matrix4 frameInA, Matrix4 frameInB, boolean useLinearReferenceFrameA) {
        super(base);
        this.frameInA = frameInA;
        this.frameInB = frameInB;
        this.useLinearReferenceFrameA = useLinearReferenceFrameA;
    }

    @Override
    public btTypedConstraint get(btRigidBody base, btRigidBody self) {
        return (base != self) ?
                new btSliderConstraint(base, self, frameInA, frameInB, useLinearReferenceFrameA) :
                new btSliderConstraint(base, frameInA, useLinearReferenceFrameA);
    }
}
