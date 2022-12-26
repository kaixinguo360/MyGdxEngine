package com.my.world.module.physics.script;

import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.physics.Constraint;
import com.my.world.module.script.ScriptSystem;

public abstract class ConstraintController extends ActivatableComponent implements ScriptSystem.OnStart, ScriptSystem.OnUpdate {

    protected Constraint constraint;

    @Override
    public void start(Scene scene, Entity entity) {
        constraint = entity.getComponent(Constraint.class);
    }

    @Override
    public void update(Scene scene, Entity entity) {
        if (constraint.btConstraint != null) {
            this.update(constraint.btConstraint);
        }
    }

    public void activate() {
        constraint.btConstraint.getRigidBodyA().activate();
        constraint.btConstraint.getRigidBodyB().activate();
    }

    public abstract void update(btTypedConstraint constraint);

}
