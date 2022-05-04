package com.my.world.module.physics;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.module.common.BaseActivatableComponent;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Constraint extends BaseActivatableComponent {

    @Config public Entity base;
    @Config public float breakingImpulseThreshold = 2000f;
    @Config public boolean disableCollisionsBetweenLinkedBodies = false;

    public btTypedConstraint btConstraint;

    public Constraint(Entity base) {
        this.base = base;
    }

    abstract public btTypedConstraint get(btRigidBody base, btRigidBody self);

    @Override
    public void dispose() {
        if (btConstraint != null) btConstraint.dispose();
    }
}
