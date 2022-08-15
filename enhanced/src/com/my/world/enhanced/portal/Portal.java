package com.my.world.enhanced.portal;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.portal.render.PortalRenderScript;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PhysicsBody;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.script.ScriptSystem;

public class Portal implements ScriptSystem.OnStart {

    @Config public String targetPortalName;
    @Config public Matrix4 targetTransform;

    public Entity selfEntity;
    public Position selfPosition;

    public Entity targetEntity;
    public Position targetPosition;
    public PortalRenderScript targetScript;

    @Override
    public void start(Scene scene, Entity entity) {

        selfEntity = entity;
        selfPosition = entity.getComponent(Position.class);

        if (targetPortalName != null) {
            targetEntity = scene.getEntityManager().findEntityByName(targetPortalName);
            targetPosition = targetEntity.getComponent(Position.class);
            targetScript = targetEntity.getComponent(PortalRenderScript.class);
        }
    }

    public Matrix4 getTargetTransform() {
        Matrix4 targetTransform;
        if (targetPosition != null) {
            targetTransform = this.targetPosition.getGlobalTransform();
        } else {
            targetTransform = this.targetTransform;
        }
        return targetTransform;
    }

    public Matrix4 getTransferTransform(Matrix4 matrix4, Matrix4 targetTransform) {
        return matrix4.set(selfPosition.getGlobalTransform()).inv().mul(targetTransform);
    }

    public void transfer(Entity entity) {

        Matrix4 realTransform = Matrix4Pool.obtain();
        Matrix4 virtualTransform = Matrix4Pool.obtain();
        Matrix4 offsetTransform = Matrix4Pool.obtain();

        Position position = entity.getComponent(Position.class);
        position.getGlobalTransform(realTransform);
        getTransferTransform(offsetTransform, getTargetTransform());
        virtualTransform.set(realTransform).mulLeft(offsetTransform);
        position.setGlobalTransform(virtualTransform);

        PhysicsBody body = entity.getComponent(PhysicsBody.class);
        if (body != null) {
            body.syncTransformFromEntity();
            if (body instanceof RigidBody) {
                btRigidBody btRigidBody = ((RigidBody) body).body;
                btRigidBody.setLinearVelocity(btRigidBody.getLinearVelocity().rot(offsetTransform));
                btRigidBody.setAngularVelocity(btRigidBody.getAngularVelocity().rot(offsetTransform));
            }
        }

        Matrix4Pool.free(realTransform);
        Matrix4Pool.free(virtualTransform);
        Matrix4Pool.free(offsetTransform);
    }
}
