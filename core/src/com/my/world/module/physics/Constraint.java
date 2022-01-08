package com.my.world.module.physics;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Component;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.util.Disposable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Constraint implements Component, Disposable {

    @Config public Entity base;

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
