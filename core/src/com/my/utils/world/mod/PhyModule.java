package com.my.utils.world.mod;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.my.utils.world.BaseModule;
import com.my.utils.world.Component;

public class PhyModule extends BaseModule<PhyComponent> {

    // Tmp Vector
    private static final Vector3 rayFrom = new Vector3();
    private static final Vector3 rayTo = new Vector3();

    // ----- Create DynamicsWorld World ----- //
    protected btDynamicsWorld dynamicsWorld;
    protected DebugDrawer debugDrawer;
    protected ClosestRayResultCallback rayTestCB;
    public PhyModule() {

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

        // Create contactListener
        MyContactListener contactListener = new MyContactListener();
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

    // ----- TODO ----- //
    protected class MyContactListener extends ContactListener {
        public boolean onContactAdded(btCollisionObject colObj0, int partId0, int index0, btCollisionObject colObj1, int partId1, int index1) {
            return true;
        }
    }

    // ----- Component ----- //
    public void addComponent(PhyComponent component) {
        dynamicsWorld.addRigidBody(component.body, component.group, component.mask);
    }
    public void removeComponent(PhyComponent component) {
        dynamicsWorld.removeRigidBody(component.body);
    }
    public boolean handle(Component component) {
        return component instanceof PhyComponent;
    }

    // ----- Custom ----- //

    // Update dynamicsWorld
    public void update() {
        dynamicsWorld.stepSimulation(Gdx.graphics.getDeltaTime(), 5, 1f/60f);
    }
    // Render DebugDrawer
    public void renderDebug(Camera cam) {
        debugDrawer.begin(cam);
        dynamicsWorld.debugDrawWorld();
        debugDrawer.end();
    }
    // Get Instance Name From PickRay
    public String pick(Camera cam, int X, int Y) {
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
            if(obj.userData instanceof PhyComponent) {
                return get((PhyComponent) obj.userData);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
