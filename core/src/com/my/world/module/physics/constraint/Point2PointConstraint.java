package com.my.world.module.physics.constraint;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.module.physics.Constraint;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Point2PointConstraint extends Constraint {

    @Config
    public Vector3 pivotInA;
    @Config
    public Vector3 pivotInB;

    public Point2PointConstraint(Entity base, Vector3 pivotInA, Vector3 pivotInB) {
        super(base);
        this.pivotInA = pivotInA;
        this.pivotInB = pivotInB;
    }

    @Override
    public btTypedConstraint get(btRigidBody base, btRigidBody self) {
        return new btPoint2PointConstraint(base, self, pivotInA, pivotInB);
    }
}
