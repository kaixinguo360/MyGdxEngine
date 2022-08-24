package com.my.world.module.physics;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.CollisionConstants;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.my.world.core.Config;
import lombok.NoArgsConstructor;

import static com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags.*;

@NoArgsConstructor
public class RigidBody extends PhysicsBody {

    @Config
    public boolean isEnableCallback = false;

    @Config
    public int callbackFlag = NORMAL_FLAG;

    @Config
    public int callbackFilter = ALL_FLAG;

    @Config
    public Integer collisionFlags;

    @Config
    public Integer activationState;

    @Config
    public boolean isStatic;

    @Config
    public boolean isKinematic;

    @Config
    public boolean autoConvertToWorldTransform = false;

    @Config
    public boolean autoConvertToLocalTransform = false;

    public btRigidBody body;

    protected RigidBody(boolean isTrigger) {
        this.isTrigger = isTrigger;
    }

    public RigidBody(btRigidBody body, boolean isTrigger) {
        this(isTrigger);
        this.body = body;
    }

    @Override
    public void enterWorld() {
        super.enterWorld();

        // Set userData
        body.userData = this;

        if (collisionFlags != null) {
            body.setCollisionFlags(collisionFlags);
        }

        if (activationState != null) {
            body.setActivationState(activationState);
        } else {
            if (isKinematic) {
                body.setActivationState(CollisionConstants.DISABLE_DEACTIVATION);
            } else {
                body.setActivationState(CollisionConstants.ACTIVE_TAG);
            }
        }

        if (isStatic) {
            body.setCollisionFlags(body.getCollisionFlags() | CF_STATIC_OBJECT);
        }

        if (isKinematic) {
            body.setCollisionFlags(body.getCollisionFlags() & ~CF_STATIC_OBJECT | CF_KINEMATIC_OBJECT);
        }

        if (isTrigger) {
            body.setCollisionFlags(body.getCollisionFlags() & ~CF_STATIC_OBJECT | CF_KINEMATIC_OBJECT | CF_NO_CONTACT_RESPONSE);
        }

        // Set Position
        if (autoConvertToWorldTransform || (!isKinematic && !isStatic)) {
            if (!position.isDisableInherit()) {
                position.disableInherit();
            }
        }
        body.proceedToTransform(position.getGlobalTransform());
        if (isKinematic) {
            body.setInterpolationWorldTransform(position.getGlobalTransform());
            body.setInterpolationLinearVelocity(Vector3.Zero);
            body.setInterpolationAngularVelocity(Vector3.Zero);
        }
        body.setMotionState(new MotionState());

        // Set OnCollision Callback
        if (isEnableCallback) {
            body.setContactCallbackFlag(callbackFlag);
            body.setContactCallbackFilter(callbackFilter);
            body.setCollisionFlags(body.getCollisionFlags() | CF_CUSTOM_MATERIAL_CALLBACK);
        }

        if (isTrigger) {
            dynamicsWorld.addCollisionObject(body, group, mask);
        } else {
            dynamicsWorld.addRigidBody(body, group, mask);
        }
    }

    @Override
    public void leaveWorld() {
        if (isTrigger) {
            dynamicsWorld.removeCollisionObject(body);
        } else {
            body.setMotionState(null);
            dynamicsWorld.removeRigidBody(body);
        }
        if (autoConvertToLocalTransform) {
            if (position.isDisableInherit()) {
                position.enableInherit();
            }
        }
        super.leaveWorld();
    }

    @Override
    public void syncTransformFromEntity() {
        body.setWorldTransform(position.getGlobalTransform());
    }

    @Override
    public void syncTransformFromWorld() {
        position.setGlobalTransform(body.getWorldTransform());
    }

    @Override
    public void dispose() {
        if (body != null) body.dispose();
    }

    private class MotionState extends btMotionState {

        @Override
        public void getWorldTransform(Matrix4 worldTrans) {
            if (position != null) {
                position.getGlobalTransform(worldTrans);
            }
        }

        @Override
        public void setWorldTransform(Matrix4 worldTrans) {
            if (position != null) {
                position.setLocalTransform(worldTrans);
            }
        }
    }
}
