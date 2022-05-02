package com.my.world.module.physics.constraint;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btFixedConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Entity;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.physics.Constraint;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConnectConstraint extends Constraint {

    public ConnectConstraint(Entity base, float breakingImpulseThreshold) {
        super(base);
        this.breakingImpulseThreshold = breakingImpulseThreshold;
    }

    @Override
    public btTypedConstraint get(btRigidBody base, btRigidBody self) {
        Matrix4 tmp1 = Matrix4Pool.obtain();
        Matrix4 tmp2 = Matrix4Pool.obtain();

        tmp1.set(base.getWorldTransform());
        tmp2.set(self.getWorldTransform());
        tmp1.inv().mul(tmp2);
        tmp2.idt();
        btFixedConstraint constraint = new btFixedConstraint(base, self, tmp1, tmp2);
        constraint.setBreakingImpulseThreshold(breakingImpulseThreshold);

        Matrix4Pool.free(tmp1);
        Matrix4Pool.free(tmp2);

        return constraint;
    }
}
