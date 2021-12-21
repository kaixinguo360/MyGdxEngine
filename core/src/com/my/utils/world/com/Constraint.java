package com.my.utils.world.com;

import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.utils.world.Component;
import com.my.utils.world.Config;
import com.my.utils.world.Entity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Constraint implements Component {

    @Config public Entity base;

    public btTypedConstraint btConstraint;

    public Constraint(Entity base) {
        this.base = base;
    }

    abstract public btTypedConstraint get(btRigidBody base, btRigidBody self);

}
