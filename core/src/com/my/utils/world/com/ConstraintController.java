package com.my.utils.world.com;

import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.sys.ScriptSystem;

public abstract class ConstraintController implements ScriptSystem.OnStart, ScriptSystem.OnUpdate {

    private Constraint constraint;

    @Override
    public void start(World world, Entity entity) {
        constraint = entity.getComponent(Constraint.class);
    }

    @Override
    public void update(World world, Entity entity) {
        if (constraint.btConstraint != null) {
            this.update(constraint.btConstraint);
        }
    }

    public abstract void update(btTypedConstraint constraint);

}
