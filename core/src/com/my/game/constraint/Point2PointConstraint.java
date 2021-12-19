package com.my.game.constraint;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btPoint2PointConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.utils.world.Config;
import com.my.utils.world.com.Constraint;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Point2PointConstraint extends Constraint {

    @Config
    public Vector3 pivotInA;
    @Config
    public Vector3 pivotInB;

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
