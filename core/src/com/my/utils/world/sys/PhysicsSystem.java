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
import com.my.utils.world.com.Collision;
import com.my.utils.world.com.Id;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;

public class PhysicsSystem extends BaseSystem {

    // ----- Tmp ----- //
    private static final Vector3 rayFrom = new Vector3();
    private static final Vector3 rayTo = new Vector3();

    // ----- Create DynamicsWorld World ----- //
    protected btDynamicsWorld dynamicsWorld;
    protected ContactListener contactListener;
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

        // Create ContactListener
        contactListener = new ContactListener();
        addDisposable(contactListener);

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

        if (entity.contain(Collision.class)) {
            Collision c = entity.get(Collision.class);
            body.setContactCallbackFlag(c.callbackFlag);
            body.setContactCallbackFilter(c.callbackFilter);
            body.setCollisionFlags(body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        }

        dynamicsWorld.addRigidBody(body, rigidBody.group, rigidBody.mask);
        activatedEntities.add(entity);
    }
    private void removeBody(Entity entity) {
        btRigidBody body = entity.get(RigidBody.class).body;
        body.setMotionState(null);
        dynamicsWorld.removeRigidBody(body);
        activatedEntities.removeValue(entity, true);
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
    private static class ContactListener extends com.badlogic.gdx.physics.bullet.collision.ContactListener {
        @Override
        public boolean onContactAdded(btCollisionObject colObj0, int partId0, int index0, boolean match0, btCollisionObject colObj1, int partId1, int index1, boolean match1) {
            if(colObj0.userData instanceof Entity && colObj1.userData instanceof Entity) {
                Entity entity0 = (Entity) colObj0.userData;
                Entity entity1 = (Entity) colObj1.userData;
                if (entity0.contain(Id.class) && entity1.contain(Id.class) && match0 && entity0.contain(Collision.class)) {
//                    System.out.println(entity0.get(Id.class) + " =>" + entity1.get(Id.class));
                    entity0.get(Collision.class).handle(entity0, entity1);
                }
                if (entity0.contain(Id.class) && entity1.contain(Id.class) && match1 && entity1.contain(Collision.class)) {
//                    System.out.println(entity1.get(Id.class) + " <= " + entity0.get(Id.class));
                    entity1.get(Collision.class).handle(entity1, entity0);
                }
            }
            return true;
        }
    }
}
