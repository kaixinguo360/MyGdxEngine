package com.my.world.module.physics;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.CollisionConstants;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.my.world.core.Config;
import com.my.world.core.util.Disposable;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags.*;

@NoArgsConstructor
public class CompoundRigidBody extends BasePhysicsBody {

    @Config
    public int group = NORMAL_FLAG;

    @Config
    public int mask = ALL_FLAG;

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
    public boolean isStatic = true;

    @Config
    public boolean isKinematic;

    @Config
    public boolean autoConvertToWorldTransform = false;

    @Config
    public boolean autoConvertToLocalTransform = false;

    public final List<btRigidBody> bodies = new ArrayList<>();

    @Override
    public void enterWorld() {
        super.enterWorld();
        bodies.forEach(this::enterWorld);
    }

    @Override
    public void leaveWorld() {
        bodies.forEach(this::leaveWorld);
        super.leaveWorld();
    }

    public void addBody(btRigidBody body) {
        bodies.add(body);
        if (this.enteredWorld) enterWorld(body);
    }

    public void removeBody(btRigidBody body) {
        if (this.enteredWorld) leaveWorld(body);
        bodies.remove(body);
    }

    protected void enterWorld(btRigidBody body) {

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

    protected void leaveWorld(btRigidBody body) {
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
    }

    @Override
    public void syncTransformFromEntity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void syncTransformFromWorld() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose() {
        Disposable.disposeAll(bodies);
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
