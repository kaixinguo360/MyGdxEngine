package com.my.world.module.physics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
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
import java.util.Map;

public class PhysicsSystem extends BaseSystem implements System.OnUpdate, Disposable, EntityListener {

    @Config
    public int maxSubSteps = 5;

    @Config
    public float fixedTimeStep = 1 / 60f;

    @Getter
    protected btDynamicsWorld dynamicsWorld;
    protected DebugDrawer debugDrawer;
    protected ClosestRayResultCallback rayTestCB;

    protected final Map<Entity, PhysicsBody> physicsBodies = new HashMap<>();
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
        return entity.contain(PhysicsBody.class) && entity.getComponent(PhysicsBody.class).isActive();
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        PhysicsBody physicsBody = entity.getComponent(PhysicsBody.class);
        physicsBody.registerToPhysicsSystem(scene, entity, this);
        physicsBody.enterWorld();
        PhysicsSystem.this.physicsBodies.put(entity, physicsBody);
    }

    @Override
    public void afterEntityRemoved(Entity entity) {
        PhysicsBody physicsBody = physicsBodies.get(entity);
        if (physicsBody != null) {
            physicsBody.leaveWorld();
            physicsBody.unregisterFromPhysicsSystem(scene, entity, this);
        }
        physicsBodies.remove(entity);
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
        for (Map.Entry<Entity, PhysicsBody> entry : physicsBodies.entrySet()) {
            PhysicsBody physicsBody = entry.getValue();
            if (!physicsBody.isTrigger && physicsBody instanceof RigidBody) {
                Entity entity = entry.getKey();
                entity.getComponent(Position.class).getGlobalTransform().getTranslation(tmpV1);
                tmpV1.sub(position);
                float len2 = tmpV1.len2();
                tmpV1.nor().scl(force * 1/len2);
                if (tmpV1.len() > MIN_FORCE) {
                    btRigidBody body = ((RigidBody) physicsBody).body;
                    body.activate();
                    body.applyCentralImpulse(tmpV1);
                }
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

    // ----- OnCollision Script ----- //

    protected final ContactListener contactListener;

    private static class ContactListener extends com.badlogic.gdx.physics.bullet.collision.ContactListener {
        @Override
        public boolean onContactAdded(btCollisionObject colObj0, int partId0, int index0, btCollisionObject colObj1, int partId1, int index1) {
            try {
                if(colObj0.userData instanceof PhysicsBody && colObj1.userData instanceof PhysicsBody) {
                    PhysicsBody body0 = (PhysicsBody) colObj0.userData;
                    PhysicsBody body1 = (PhysicsBody) colObj1.userData;
                    body0.collision(body1);
                    body1.collision(body0);
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
