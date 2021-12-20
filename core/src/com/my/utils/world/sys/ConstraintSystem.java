package com.my.utils.world.sys;

import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.utils.world.System;
import com.my.utils.world.*;
import com.my.utils.world.com.Constraint;
import com.my.utils.world.com.RigidBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.badlogic.gdx.physics.bullet.dynamics.btConstraintParams.BT_CONSTRAINT_CFM;
import static com.badlogic.gdx.physics.bullet.dynamics.btConstraintParams.BT_CONSTRAINT_ERP;

public class ConstraintSystem extends BaseSystem implements EntityListener, System.OnStart {

    private final List<ConstraintInner> constraintInners = new ArrayList<>();
    private btDynamicsWorld dynamicsWorld;

    @Override
    public void start(World world) {
        dynamicsWorld = world.getSystemManager().getSystem(PhysicsSystem.class).dynamicsWorld;
    }

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contains(Constraint.class);
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        List<Constraint> constraintList = entity.getComponents(Constraint.class);
        for (Constraint constraint : constraintList) {
            ConstraintInner constraintInner = new ConstraintInner();
            constraintInner.entity = entity;
            constraintInner.constraint = constraint;
            btRigidBody self = entity.getComponent(RigidBody.class).body;
            btRigidBody base = constraint.base.getComponent(RigidBody.class).body;
            constraint.btConstraint = constraint.get(base, self);
            constraint.btConstraint.setParam(BT_CONSTRAINT_CFM, 0);
            constraint.btConstraint.setParam(BT_CONSTRAINT_ERP, 0.5f);
            dynamicsWorld.addConstraint(constraint.btConstraint);
            constraintInners.add(constraintInner);
        }
    }

    @Override
    public void afterEntityRemoved(Entity entity) {
        btDynamicsWorld dynamicsWorld = world.getSystemManager().getSystem(PhysicsSystem.class).dynamicsWorld;
        Iterator<ConstraintInner> it = constraintInners.iterator();
        while (it.hasNext()) {
            ConstraintInner constraintInner = it.next();
            if ((constraintInner.entity == entity || constraintInner.constraint.base == entity)) {
                if (constraintInner.constraint.btConstraint != null) {
                    dynamicsWorld.removeConstraint(constraintInner.constraint.btConstraint);
                    constraintInner.constraint.btConstraint.dispose();
                    constraintInner.constraint.btConstraint = null;
                }
                it.remove();
            }
        }
    }

    @Override
    public void dispose() {
        btDynamicsWorld dynamicsWorld = world.getSystemManager().getSystem(PhysicsSystem.class).dynamicsWorld;
        Iterator<ConstraintInner> it = constraintInners.iterator();
        while (it.hasNext()) {
            ConstraintInner constraintInner = it.next();
            if (constraintInner.constraint.btConstraint != null) {
                dynamicsWorld.removeConstraint(constraintInner.constraint.btConstraint);
                constraintInner.constraint.btConstraint.dispose();
                constraintInner.constraint.btConstraint = null;
            }
            it.remove();
        }
        super.dispose();
    }

    private static class ConstraintInner {
        private Entity entity;
        private Constraint constraint;
    }
}
