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
import com.my.utils.world.AssetsManager;
import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.com.Collision;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;
import lombok.Getter;
import lombok.Setter;

public class PhysicsSystem extends BaseSystem {

    @Getter
    @Setter
    private AssetsManager assetsManager;

    // ----- Tmp ----- //
    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();

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
    public boolean isHandleable(Entity entity) {
        return entity.contain(Position.class, RigidBody.class);
    }

    // ----- Custom ----- //

    protected final Array<Entity> activatedEntities = new Array<>();
    // Update dynamicsWorld
    public void update(float deltaTime) {
        for (Entity entity : getEntities()) {
            if (!activatedEntities.contains(entity, true)) {
                addBody(entity);
            }
        }
        for (int i = activatedEntities.size - 1; i >= 0; i--) {
            Entity entity = activatedEntities.get(i);
            if (!getEntities().contains(entity)) {
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

        Vector3 rayFrom = tmpV1;
        Vector3 rayTo = tmpV2;

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
    // Add Explosion
    private static final float MIN_FORCE = 10;
    public void addExplosion(Vector3 position, float force) {
        for (Entity entity : activatedEntities) {
            entity.getComponent(Position.class).transform.getTranslation(tmpV1);
            tmpV1.sub(position);
            float len2 = tmpV1.len2();
            tmpV1.nor().scl(force * 1/len2);
            if (tmpV1.len() > MIN_FORCE) {
                entity.getComponent(RigidBody.class).body.activate();
                entity.getComponent(RigidBody.class).body.applyCentralImpulse(tmpV1);
            }
        }
    }

    // ----- Private ----- //
    private void addBody(Entity entity) {
        Position position = entity.getComponent(Position.class);
        RigidBody rigidBody = entity.getComponent(RigidBody.class);

        btRigidBody body = rigidBody.body;
        body.proceedToTransform(position.transform);
        body.setMotionState(new MotionState(position.transform));
        body.userData = entity;

        if (entity.contain(Collision.class)) {
            Collision c = entity.getComponent(Collision.class);
            body.setContactCallbackFlag(c.callbackFlag);
            body.setContactCallbackFilter(c.callbackFilter);
            body.setCollisionFlags(body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        }

        dynamicsWorld.addRigidBody(body, rigidBody.group, rigidBody.mask);
        activatedEntities.add(entity);
    }
    private void removeBody(Entity entity) {
        btRigidBody body = entity.getComponent(RigidBody.class).body;
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
                if (match0 && entity0.contain(Collision.class)) {
//                    System.out.println(entity0.getId() + " =>" + entity1.getId());
                    Collision collision = entity0.getComponent(Collision.class);
                    CollisionHandler handler = collision.handler;
                    handler.handle(entity0, entity1);
                }
                if (match1 && entity1.contain(Collision.class)) {
//                    System.out.println(entity1.getId() + " <= " + entity0.getId());
                    Collision collision = entity1.getComponent(Collision.class);
                    CollisionHandler handler = collision.handler;
                    handler.handle(entity1, entity0);
                }
            }
            return true;
        }
    }

    // ----- Assets ----- //

    public static class RigidBodyConfig {

        // ----- Static ----- //
        public final static short STATIC_FLAG = 1 << 8;
        public final static short NORMAL_FLAG = 1 << 9;
        public final static short ALL_FLAG = -1;
        private static final Vector3 localInertia = new Vector3();

        public final btRigidBody.btRigidBodyConstructionInfo constructionInfo;
        public final int group;
        public final int mask;

        public RigidBodyConfig(btCollisionShape shape, float mass) {
            this(shape, mass, NORMAL_FLAG, ALL_FLAG);
        }

        public RigidBodyConfig(btCollisionShape shape, float mass, int group, int mask) {
            this.group = group;
            this.mask = mask;
            if (mass > 0f)
                shape.calculateLocalInertia(mass, localInertia);
            else
                localInertia.set(0, 0, 0);
            this.constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
        }

        public RigidBody newInstance() {
            RigidBody rigidBody = new RigidBody();
            rigidBody.body = new btRigidBody(constructionInfo);
            rigidBody.group = group;
            rigidBody.mask = mask;
            rigidBody.bodyConfig = this;
            return rigidBody;
        }
    }

    public interface CollisionHandler {
        void handle(Entity self, Entity target);
    }

}
