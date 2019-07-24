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
import com.badlogic.gdx.utils.Array;
import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;

public class PhysicsSystem extends BaseSystem {

    // ----- Tmp ----- //
    private static final Vector3 rayFrom = new Vector3();
    private static final Vector3 rayTo = new Vector3();

    // ----- Create DynamicsWorld World ----- //
    protected btDynamicsWorld dynamicsWorld;
    protected DebugDrawer debugDrawer;
    protected ClosestRayResultCallback rayTestCB;
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
    }

    // ----- Check ----- //
    public boolean check(Entity entity) {
        return entity.contain(Position.class, RigidBody.class);
    }

    // ----- Custom ----- //

    protected final Array<Entity> activatedEntities = new Array<>();
    // Update dynamicsWorld
    public void update(float deltaTime) {
        for (Entity entity : entities) {
            if (!activatedEntities.contains(entity, true)) {
                addBody(entity);
            }
        }
        for (int i = activatedEntities.size - 1; i >= 0; i--) {
            Entity entity = activatedEntities.get(i);
            if (!entities.contains(entity, true)) {
                removeBody(entity);
            }
        }
        dynamicsWorld.stepSimulation(deltaTime);
    }
    // Render DebugDrawer
    public void renderDebug(Camera cam) {
        debugDrawer.begin(cam);
        dynamicsWorld.debugDrawWorld();
        debugDrawer.end();
    }
    // Set DebugMode
    public void setDebugMode(int debugMode) {
        debugDrawer.setDebugMode(debugMode);
    }
    // Get Instance Name From PickRay
    public Entity pick(Camera cam, int X, int Y) {
        Ray ray = cam.getPickRay(X, Y);

        rayFrom.set(ray.origin);
        rayTo.set(ray.origin).add(ray.direction.cpy().scl(100)); // 50 meters max from the origin

        // Because we reuse the ClosestRayResultCallback, we need reset it's values
        rayTestCB.setCollisionObject(null);
        rayTestCB.setClosestHitFraction(1f);
        rayTestCB.setRayFromWorld(rayFrom);
        rayTestCB.setRayToWorld(rayTo);

        dynamicsWorld.rayTest(rayFrom, rayTo, rayTestCB);

        if (rayTestCB.hasHit()) {
            final btCollisionObject obj = rayTestCB.getCollisionObject();
            assert obj.userData instanceof Entity;
            return (Entity) obj.userData;
        } else {
            return null;
        }
    }

    // ----- Private ----- //
    private void addBody(Entity entity) {
        Position position = entity.get(Position.class);
        RigidBody rigidBody = entity.get(RigidBody.class);

        btRigidBody body = rigidBody.body;
        body.proceedToTransform(position.transform);
        body.setMotionState(new MotionState(position.transform));
        body.userData = entity;

        dynamicsWorld.addRigidBody(body, rigidBody.group, rigidBody.mask);
        activatedEntities.add(entity);
    }
    private void removeBody(Entity entity) {
        btRigidBody body = entity.get(RigidBody.class).body;
        body.setMotionState(null);
        dynamicsWorld.removeRigidBody(body);
        activatedEntities.removeValue(entity, true);
    }
    class MotionState extends btMotionState {
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
}
