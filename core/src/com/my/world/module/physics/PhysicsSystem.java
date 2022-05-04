package com.my.world.module.physics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.my.world.core.System;
import com.my.world.core.*;
import com.my.world.core.util.Disposable;
import com.my.world.gdx.DisposableManager;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.common.Position;
import com.my.world.module.common.Script;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.badlogic.gdx.physics.bullet.collision.btCollisionObject.CollisionFlags.*;

public class PhysicsSystem extends BaseSystem implements System.OnUpdate, Disposable, EntityListener {

    @Config
    public int maxSubSteps = 5;

    @Config
    public float fixedTimeStep = 1 / 60f;

    @Getter
    protected btDynamicsWorld dynamicsWorld;
    protected DebugDrawer debugDrawer;
    protected ClosestRayResultCallback rayTestCB;

    protected final Map<Entity, RigidBody> rigidBodies = new HashMap<>();
    protected final DisposableManager disposableManager = new DisposableManager();

    public PhysicsSystem() {

        // ----- Create DynamicsWorld ----- //

        // Create collisionConfig
        btCollisionConfiguration collisionConfig = new btDefaultCollisionConfiguration();
        disposableManager.addDisposable(collisionConfig);

        // Create dispatcher
        btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfig);
        btGImpactCollisionAlgorithm.registerAlgorithm(dispatcher);
        disposableManager.addDisposable(dispatcher);

        // Create broadphase
        btDbvtBroadphase broadphase = new btDbvtBroadphase();
        disposableManager.addDisposable(broadphase);

        // Create btGhostPairCallback
        btGhostPairCallback ghostPairCallback = new btGhostPairCallback();
        broadphase.getOverlappingPairCache().setInternalGhostPairCallback(ghostPairCallback);
        disposableManager.addDisposable(ghostPairCallback);

        // Create constraintSolver
        btConstraintSolver constraintSolver = new btSequentialImpulseConstraintSolver();
        disposableManager.addDisposable(constraintSolver);

        // Create dynamicsWorld
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -10f, 0));
        disposableManager.addDisposable(dynamicsWorld);

        // Create debugDrawer
        debugDrawer = new DebugDrawer();
        debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
        dynamicsWorld.setDebugDrawer(debugDrawer);
        disposableManager.addDisposable(debugDrawer);

        // Create rayTestCB
        rayTestCB = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);
        disposableManager.addDisposable(rayTestCB);

        // OnCollision Script
        contactListener = new ContactListener();
        disposableManager.addDisposable(contactListener);

        // OnFixedUpdate Script
        preTickListener = new PreTickListener(dynamicsWorld);
        disposableManager.addDisposable(preTickListener);
    }

    @Override
    public void afterAdded(Scene scene) {
        super.afterAdded(scene);
        scene.getEntityManager().addFilter(onFixedUpdateFilter);
    }

    @Override
    public boolean canHandle(Entity entity) {
        return entity.contain(RigidBody.class) && entity.getComponent(RigidBody.class).isActive();
    }

    @Override
    public void afterEntityAdded(Entity entity) {

        // Get RigidBody
        RigidBody rigidBody = entity.getComponent(RigidBody.class);
        rigidBody.system = this;
        btRigidBody body = rigidBody.body;
        body.userData = entity;

        if (rigidBody.collisionFlags != null) {
            body.setCollisionFlags(rigidBody.collisionFlags);
        }

        if (rigidBody.isStatic) {
            body.setCollisionFlags(body.getCollisionFlags() | CF_STATIC_OBJECT);
        }

        if (rigidBody.isKinematic) {
            body.setCollisionFlags(body.getCollisionFlags() & ~CF_STATIC_OBJECT | CF_KINEMATIC_OBJECT);
        }

        if (rigidBody.isTrigger) {
            body.setCollisionFlags(body.getCollisionFlags() & ~CF_STATIC_OBJECT | CF_KINEMATIC_OBJECT | CF_NO_CONTACT_RESPONSE);
        }

        // Set Position
        Position position = entity.getComponent(Position.class);
        if (rigidBody.autoConvertToWorldTransform || (!rigidBody.isKinematic && !rigidBody.isStatic)) {
            if (!position.isDisableInherit()) {
                position.disableInherit();
            }
        }
        body.proceedToTransform(position.getGlobalTransform());
        if (rigidBody.isKinematic) {
            body.setInterpolationWorldTransform(position.getGlobalTransform());
            body.setInterpolationLinearVelocity(Vector3.Zero);
            body.setInterpolationAngularVelocity(Vector3.Zero);
        }
        body.setMotionState(new MotionState(position));

        // Set OnCollision Callback
        if (entity.contain(Collision.class)) {
            Collision c = entity.getComponent(Collision.class);
            body.setContactCallbackFlag(c.callbackFlag);
            body.setContactCallbackFilter(c.callbackFilter);
            body.setCollisionFlags(body.getCollisionFlags() | CF_CUSTOM_MATERIAL_CALLBACK);
        }

        // Add to World
        if (rigidBody.isTrigger) {
            dynamicsWorld.addCollisionObject(body, rigidBody.group, rigidBody.mask);
        } else {
            dynamicsWorld.addRigidBody(body, rigidBody.group, rigidBody.mask);
        }

        // Add to List
        PhysicsSystem.this.rigidBodies.put(entity, rigidBody);
    }

    @Override
    public void afterEntityRemoved(Entity entity) {
        RigidBody rigidBody = rigidBodies.get(entity);
        if (rigidBody != null) {
            if (rigidBody.isTrigger) {
                dynamicsWorld.removeCollisionObject(rigidBody.body);
            } else {
                btRigidBody body = rigidBody.body;
                body.setMotionState(null);
                dynamicsWorld.removeRigidBody(body);
            }
            if (rigidBody.autoConvertToLocalTransform) {
                Position position = entity.getComponent(Position.class);
                if (position != null && position.isDisableInherit()) {
                    position.enableInherit();
                }
            }
            rigidBody.system = null;
        }
        rigidBodies.remove(entity);
    }

    @Override
    public void update(float deltaTime) {
        dynamicsWorld.stepSimulation(deltaTime, maxSubSteps, fixedTimeStep);
    }

    @Override
    public void dispose() {
        this.disposableManager.dispose();
        dynamicsWorld = null;
        debugDrawer = null;
        rayTestCB = null;
        scene = null;
        maxSubSteps = 5;
        fixedTimeStep = 1 / 60f;
    }

    // ----- Custom ----- //

    // Render DebugDrawer
    public void renderDebug(Camera cam) {
        debugDrawer.begin(cam);
        dynamicsWorld.debugDrawWorld();
        debugDrawer.end();
    }

    // Set Debug Mode
    public void setDebugMode(int debugMode) {
        debugDrawer.setDebugMode(debugMode);
    }

    // Get Instance Name From PickRay
    public Entity pick(Camera cam, int X, int Y, float maxLength) {
        Ray ray = cam.getPickRay(X, Y);

        Vector3 rayFrom = Vector3Pool.obtain();
        Vector3 rayTo = Vector3Pool.obtain();
        Vector3 tmpV = Vector3Pool.obtain();

        rayFrom.set(ray.origin);
        rayTo.set(ray.origin).add(tmpV.set(ray.direction).scl(maxLength)); // 50 meters max from the origin

        // Because we reuse the ClosestRayResultCallback, we need reset it's values
        rayTestCB.setCollisionObject(null);
        rayTestCB.setClosestHitFraction(1f);
        rayTestCB.setRayFromWorld(rayFrom);
        rayTestCB.setRayToWorld(rayTo);

        dynamicsWorld.rayTest(rayFrom, rayTo, rayTestCB);

        Vector3Pool.free(rayFrom);
        Vector3Pool.free(rayTo);
        Vector3Pool.free(tmpV);

        if (rayTestCB.hasHit()) {
            final btCollisionObject obj = rayTestCB.getCollisionObject();
            assert obj.userData instanceof Entity;
            return (Entity) obj.userData;
        } else {
            return null;
        }
    }

    // Add Explosion
    private static final float MIN_FORCE = 10;
    public void addExplosion(Vector3 position, float force) {
        Vector3 tmpV1 = Vector3Pool.obtain();
        for (Map.Entry<Entity, RigidBody> entry : rigidBodies.entrySet()) {
            RigidBody rigidBody = entry.getValue();
            if (rigidBody.isTrigger) continue;
            Entity entity = entry.getKey();
            entity.getComponent(Position.class).getGlobalTransform().getTranslation(tmpV1);
            tmpV1.sub(position);
            float len2 = tmpV1.len2();
            tmpV1.nor().scl(force * 1/len2);
            if (tmpV1.len() > MIN_FORCE) {
                rigidBody.body.activate();
                rigidBody.body.applyCentralImpulse(tmpV1);
            }
        }
        Vector3Pool.free(tmpV1);
    }

    // Get Rigid Body Construction Info
    private static final Vector3 localInertia = new Vector3();
    public static btRigidBody.btRigidBodyConstructionInfo getRigidBodyConfig(btCollisionShape shape, float mass) {
        if (mass > 0f) {
            shape.calculateLocalInertia(mass, localInertia);
        } else {
            localInertia.set(0, 0, 0);
        }
        return new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
    }

    private static class MotionState extends btMotionState {

        private final Position position;

        private MotionState(Position position) {
            this.position = position;
        }

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

    // ----- OnCollision Script ----- //

    protected final ContactListener contactListener;

    private static class ContactListener extends com.badlogic.gdx.physics.bullet.collision.ContactListener {
        @Override
        public boolean onContactAdded(btCollisionObject colObj0, int partId0, int index0, boolean match0, btCollisionObject colObj1, int partId1, int index1, boolean match1) {
            try {
                if(colObj0.userData instanceof Entity && colObj1.userData instanceof Entity) {
                    Entity entity0 = (Entity) colObj0.userData;
                    Entity entity1 = (Entity) colObj1.userData;
                    if (match0 && entity1.contains(RigidBody.class)) {
                        RigidBody rigidBody = entity1.getComponent(RigidBody.class);
                        if (rigidBody == null || !rigidBody.isTrigger) {
                            List<OnCollision> scripts = entity0.getComponents(OnCollision.class);
                            for (OnCollision script : scripts) {
                                if (Component.isActive(script)) {
                                    script.collision(entity1);
                                }
                            }
                        }
                    }
                    if (match1) {
                        RigidBody rigidBody = entity0.getComponent(RigidBody.class);
                        if (rigidBody == null || !rigidBody.isTrigger) {
                            List<OnCollision> scripts = entity1.getComponents(OnCollision.class);
                            for (OnCollision script : scripts) {
                                if (Component.isActive(script)) {
                                    script.collision(entity0);
                                }
                            }
                        }
                    }
                }
                return true;
            } catch (Throwable e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public interface OnCollision extends Script {
        void collision(Entity entity);
    }

    // ----- OnFixedUpdate Script ----- //

    protected final EntityFilter onFixedUpdateFilter = entity -> entity.contain(OnFixedUpdate.class);
    protected final PreTickListener preTickListener;

    private class PreTickListener extends InternalTickCallback {

        private PreTickListener(btDynamicsWorld dynamicsWorld) {
            super(dynamicsWorld, true);
        }

        @Override
        public void onInternalTick(btDynamicsWorld dynamicsWorld, float timeStep) {
            for (Entity entity : scene.getEntityManager().getEntitiesByFilter(onFixedUpdateFilter)) {
                for (OnFixedUpdate script : entity.getComponents(OnFixedUpdate.class)) {
                    if (Component.isActive(script)) {
                        script.fixedUpdate(scene, dynamicsWorld, entity);
                    }
                }
            }
        }
    }

    public interface OnFixedUpdate extends Script {
        void fixedUpdate(Scene scene, btDynamicsWorld dynamicsWorld, Entity entity);
    }

}
