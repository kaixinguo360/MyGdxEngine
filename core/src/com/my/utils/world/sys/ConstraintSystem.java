package com.my.utils.world.sys;

import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.badlogic.gdx.utils.Array;
import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityListener;
import com.my.utils.world.com.Constraint;
import com.my.utils.world.com.RigidBody;

import java.util.Iterator;
import java.util.Map;

import static com.badlogic.gdx.physics.bullet.dynamics.btConstraintParams.BT_CONSTRAINT_CFM;
import static com.badlogic.gdx.physics.bullet.dynamics.btConstraintParams.BT_CONSTRAINT_ERP;

public class ConstraintSystem extends BaseSystem implements EntityListener {

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(Constraint.class);
    }

    @Override
    public void afterAdded(Entity entity) {
        Constraint constraint = entity.getComponent(Constraint.class);
        ConstraintInner constraintInner = new ConstraintInner();
        constraintInner.bodyA = constraint.bodyA;
        constraintInner.bodyB = constraint.bodyB;
        constraintInner.type = constraint.type;
        constraintInner.config = constraint.config;
        constraintInner.controller = constraint.controller;
        constraintInners.add(constraintInner);
    }

    @Override
    public void afterRemoved(Entity entity) {
        String body = entity.getId();
        btDynamicsWorld dynamicsWorld = world.getSystemManager().getSystem(PhysicsSystem.class).dynamicsWorld;
        Iterator<ConstraintInner> it = constraintInners.iterator();
        while (it.hasNext()) {
            ConstraintInner constraintInner = it.next();
            if ((constraintInner.bodyA.equals(body) || constraintInner.bodyB.equals(body))) {
                if (constraintInner.btConstraint != null) {
                    dynamicsWorld.removeConstraint(constraintInner.btConstraint);
                    constraintInner.btConstraint.dispose();
                    constraintInner.btConstraint = null;
                }
                it.remove();
            }
        }
    }

    @Override
    public void dispose() {
        btDynamicsWorld dynamicsWorld = world.getSystemManager().getSystem(PhysicsSystem.class).dynamicsWorld;
        for (ConstraintInner constraintInner : constraintInners) {
            if (constraintInner.btConstraint != null) {
                dynamicsWorld.removeConstraint(constraintInner.btConstraint);
                constraintInner.btConstraint.dispose();
                constraintInner.btConstraint = null;
            }
        }
        super.dispose();
    }

    // ----- Custom ----- //
    private final Array<ConstraintInner> constraintInners = new Array<>();

    public void update() {
        btDynamicsWorld dynamicsWorld = world.getSystemManager().getSystem(PhysicsSystem.class).dynamicsWorld;
        for (ConstraintInner constraintInner : constraintInners) {
            if (constraintInner.btConstraint == null) {
                btRigidBody bodyA = world.getEntityManager().getEntity(constraintInner.bodyA).getComponent(RigidBody.class).body;
                btRigidBody bodyB = world.getEntityManager().getEntity(constraintInner.bodyB).getComponent(RigidBody.class).body;
                constraintInner.btConstraint = constraintInner.type.get(bodyA, bodyB, constraintInner.config);
                constraintInner.btConstraint.setParam(BT_CONSTRAINT_CFM, 0);
                constraintInner.btConstraint.setParam(BT_CONSTRAINT_ERP, 0.5f);
                dynamicsWorld.addConstraint(constraintInner.btConstraint);
            }
            if (constraintInner.btConstraint != null && constraintInner.controller != null) {
                constraintInner.controller.update(constraintInner.btConstraint);
            }
        }
    }

    private static class ConstraintInner {
        private String bodyA;
        private String bodyB;
        private ConstraintType type;
        private Map<String, Object> config;
        private ConstraintController controller;
        private btTypedConstraint btConstraint;
    }
    public interface ConstraintController {
        void update(btTypedConstraint constraint);
    }
    public interface ConstraintType {
        btTypedConstraint get(btRigidBody bodyA, btRigidBody bodyB, Map<String, Object> config);
    }
}
