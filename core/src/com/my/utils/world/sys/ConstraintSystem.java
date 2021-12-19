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

    private final List<Constraint> constraints = new ArrayList<>();

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contains(Constraint.class);
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        constraints.addAll(entity.getComponents(Constraint.class));
    }

    @Override
    public void afterEntityRemoved(Entity entity) {
        String body = entity.getId();
        btDynamicsWorld dynamicsWorld = world.getSystemManager().getSystem(PhysicsSystem.class).dynamicsWorld;
        Iterator<Constraint> it = constraints.iterator();
        while (it.hasNext()) {
            Constraint constraint = it.next();
            if ((constraint.bodyA.equals(body) || constraint.bodyB.equals(body))) {
                if (constraint.btConstraint != null) {
                    dynamicsWorld.removeConstraint(constraint.btConstraint);
                    constraint.btConstraint.dispose();
                    constraint.btConstraint = null;
                }
                it.remove();
            }
        }
    }

    @Override
    public void start(World world) {
        btDynamicsWorld dynamicsWorld = world.getSystemManager().getSystem(PhysicsSystem.class).dynamicsWorld;
        for (Constraint constraint : constraints) {
            btRigidBody bodyA = world.getEntityManager().getEntity(constraint.bodyA).getComponent(RigidBody.class).body;
            btRigidBody bodyB = world.getEntityManager().getEntity(constraint.bodyB).getComponent(RigidBody.class).body;
            constraint.btConstraint = constraint.get(bodyA, bodyB);
            constraint.btConstraint.setParam(BT_CONSTRAINT_CFM, 0);
            constraint.btConstraint.setParam(BT_CONSTRAINT_ERP, 0.5f);
            dynamicsWorld.addConstraint(constraint.btConstraint);
        }
    }

    @Override
    public void dispose() {
        btDynamicsWorld dynamicsWorld = world.getSystemManager().getSystem(PhysicsSystem.class).dynamicsWorld;
        Iterator<Constraint> it = constraints.iterator();
        while (it.hasNext()) {
            Constraint constraint = it.next();
            if (constraint.btConstraint != null) {
                dynamicsWorld.removeConstraint(constraint.btConstraint);
                constraint.btConstraint.dispose();
                constraint.btConstraint = null;
            }
            it.remove();
        }
        super.dispose();
    }
}
