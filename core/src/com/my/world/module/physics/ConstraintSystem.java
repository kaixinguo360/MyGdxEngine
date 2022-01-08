package com.my.world.module.physics;

import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.world.core.Entity;
import com.my.world.core.EntityListener;
import com.my.world.core.Scene;
import com.my.world.core.System;
import com.my.world.core.util.Disposable;
import com.my.world.module.common.BaseSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.badlogic.gdx.physics.bullet.dynamics.btConstraintParams.BT_CONSTRAINT_CFM;
import static com.badlogic.gdx.physics.bullet.dynamics.btConstraintParams.BT_CONSTRAINT_ERP;

public class ConstraintSystem extends BaseSystem implements EntityListener, System.OnStart, Disposable {

    private final List<ConstraintInner> constraintInners = new ArrayList<>();
    private btDynamicsWorld dynamicsWorld;

    @Override
    public void start(Scene scene) {
        dynamicsWorld = scene.getSystemManager().getSystem(PhysicsSystem.class).dynamicsWorld;
    }

    @Override
    public void dispose() {
        for (ConstraintInner constraintInner : constraintInners) {
            constraintInner.constraint = null;
            constraintInner.entity = null;
        }
        constraintInners.clear();
        dynamicsWorld = null;
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
        btDynamicsWorld dynamicsWorld = scene.getSystemManager().getSystem(PhysicsSystem.class).dynamicsWorld;
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

    private static class ConstraintInner {
        private Entity entity;
        private Constraint constraint;
    }
}
