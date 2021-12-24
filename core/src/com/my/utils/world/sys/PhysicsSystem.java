package com.my.utils.world.sys;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.my.utils.world.System;
import com.my.utils.world.*;
import com.my.utils.world.com.Collision;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.com.Script;
import com.my.utils.world.util.pool.Matrix4Pool;
import com.my.utils.world.util.pool.Vector3Pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PhysicsSystem extends BaseSystem implements EntityListener, System.OnUpdate {

    @Config
    public int maxSubSteps = 5;

    @Config
    public float fixedTimeStep = 1 / 60f;

    protected btDynamicsWorld dynamicsWorld;
    protected DebugDrawer debugDrawer;
    protected ClosestRayResultCallback rayTestCB;
    protected final List<RigidBodyInner> rigidBodyInners = new ArrayList<>();

    public PhysicsSystem() {

        // ----- Create DynamicsWorld ----- //

        // Create collisionConfig
        btCollisionConfiguration collisionConfig = new btDefaultCollisionConfiguration();
        addDisposable(collisionConfig);

        // Create dispatcher
        btCollisionDispatcher dispatcher = new btCollisionDispatcher(collisionConfig);
        btGImpactCollisionAlgorithm.registerAlgorithm(dispatcher);
        addDisposable(dispatcher);

        // Create broadphase
        btDbvtBroadphase broadphase = new btDbvtBroadphase();
        addDisposable(broadphase);

        // Create constraintSolver
        btConstraintSolver constraintSolver = new btSequentialImpulseConstraintSolver();
        addDisposable(constraintSolver);

        // Create dynamicsWorld
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -10f, 0));
        addDisposable(dynamicsWorld);

        // Create debugDrawer
        debugDrawer = new DebugDrawer();
        debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
        dynamicsWorld.setDebugDrawer(debugDrawer);
        addDisposable(debugDrawer);

        // Create rayTestCB
        rayTestCB = new ClosestRayResultCallback(Vector3.Zero, Vector3.Z);
        addDisposable(rayTestCB);

        // OnCollision Script
        contactListener = new ContactListener();
        addDisposable(contactListener);

        // OnFixedUpdate Script
        preTickListener = new PreTickListener(dynamicsWorld);
        addDisposable(preTickListener);
    }

    @Override
    public void afterAdded(World world) {
        super.afterAdded(world);
        world.getEntityManager().addFilter(onFixedUpdateFilter);
    }

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(Position.class, RigidBody.class);
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        RigidBodyInner rigidBodyInner = new RigidBodyInner();

        rigidBodyInner.entity = entity;
        rigidBodyInner.position = entity.getComponent(Position.class);
        rigidBodyInner.rigidBody = entity.getComponent(RigidBody.class);

        if (!rigidBodyInner.position.isDisableInherit()) {
            Matrix4 tmpM = Matrix4Pool.obtain();
            rigidBodyInner.position.getGlobalTransform(tmpM);
            rigidBodyInner.position.setLocalTransform(tmpM);
            rigidBodyInner.position.setDisableInherit(true);
            Matrix4Pool.free(tmpM);
        }

        btRigidBody body = rigidBodyInner.rigidBody.body;
        body.proceedToTransform(rigidBodyInner.position.getLocalTransform());
        body.setMotionState(new MotionState(rigidBodyInner.position.getLocalTransform()));
        body.userData = entity;

        if (entity.contains(Collision.class)) {
            Collision c = entity.getComponent(Collision.class);
            body.setContactCallbackFlag(c.callbackFlag);
            body.setContactCallbackFilter(c.callbackFilter);
            body.setCollisionFlags(body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        }

        dynamicsWorld.addRigidBody(body, rigidBodyInner.rigidBody.group, rigidBodyInner.rigidBody.mask);

        rigidBodyInners.add(rigidBodyInner);
    }

    @Override
    public void afterEntityRemoved(Entity entity) {
        Iterator<RigidBodyInner> it = rigidBodyInners.iterator();
        while (it.hasNext()) {
            RigidBodyInner rigidBodyInner = it.next();
            if (rigidBodyInner.entity == entity) {
                btRigidBody body = rigidBodyInner.rigidBody.body;
                body.setMotionState(null);
                dynamicsWorld.removeRigidBody(body);
                it.remove();
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        dynamicsWorld.stepSimulation(deltaTime, maxSubSteps, fixedTimeStep);
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
        for (RigidBodyInner rigidBodyInner : rigidBodyInners) {
            rigidBodyInner.position.getLocalTransform().getTranslation(tmpV1);
            tmpV1.sub(position);
            float len2 = tmpV1.len2();
            tmpV1.nor().scl(force * 1/len2);
            if (tmpV1.len() > MIN_FORCE) {
                rigidBodyInner.rigidBody.body.activate();
                rigidBodyInner.rigidBody.body.applyCentralImpulse(tmpV1);
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

    // ----- Private ----- //

    private static class RigidBodyInner {
        private Entity entity;
        private RigidBody rigidBody;
        private Position position;
    }
    private static class MotionState extends btMotionState {
        Matrix4 transform;
        MotionState(Matrix4 transform) {
            this.transform = transform;
        }
        @Override
        public void getWorldTransform (Matrix4 worldTrans) {
            if (transform != null) worldTrans.set(transform);
        }
        @Override
        public void setWorldTransform (Matrix4 worldTrans) {
            if (transform != null) transform.set(worldTrans);
        }
    }

    // ----- OnCollision Script ----- //

    protected final ContactListener contactListener;

    private static class ContactListener extends com.badlogic.gdx.physics.bullet.collision.ContactListener {
        @Override
        public boolean onContactAdded(btCollisionObject colObj0, int partId0, int index0, boolean match0, btCollisionObject colObj1, int partId1, int index1, boolean match1) {
            if(colObj0.userData instanceof Entity && colObj1.userData instanceof Entity) {
                Entity entity0 = (Entity) colObj0.userData;
                Entity entity1 = (Entity) colObj1.userData;
                if (match0) {
//                    System.out.println(entity0.getId() + " =>" + entity1.getId());
                    List<OnCollision> scripts = entity0.getComponents(OnCollision.class);
                    for (OnCollision script : scripts) {
                        script.collision(entity1);
                    }
                }
                if (match1) {
//                    System.out.println(entity1.getId() + " <= " + entity0.getId());
                    List<OnCollision> scripts = entity1.getComponents(OnCollision.class);
                    for (OnCollision script : scripts) {
                        script.collision(entity0);
                    }
                }
            }
            return true;
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
            for (Entity entity : world.getEntityManager().getEntitiesByFilter(onFixedUpdateFilter)) {
                for (OnFixedUpdate script : entity.getComponents(OnFixedUpdate.class)) {
                    script.fixedUpdate(world, dynamicsWorld, entity);
                }
            }
        }
    }

    public interface OnFixedUpdate extends Script {
        void fixedUpdate(World world, btDynamicsWorld dynamicsWorld, Entity entity);
    }

}
