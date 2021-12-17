package com.my.utils.world.com;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.utils.world.Component;
import com.my.utils.world.Config;
import com.my.utils.world.StandaloneResource;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Constraint implements Component, StandaloneResource {

    @Config
    public String bodyA;

    @Config
    public String bodyB;

    @Config(isPrimitive = false)
    public ConstraintController controller;

    public btTypedConstraint btConstraint;

    public Constraint(String bodyA, String bodyB, ConstraintController controller) {
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        this.controller = controller;
    }

    abstract public btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB);

    public interface ConstraintController {
        void update(btTypedConstraint constraint);
    }
}
