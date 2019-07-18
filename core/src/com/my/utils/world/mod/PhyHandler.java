package com.my.utils.world.mod;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.BaseModule;
import com.my.utils.world.Handler;
import com.my.utils.world.Module;

import java.util.HashMap;
import java.util.Map;

public class PhyHandler extends BaseModule<btRigidBody> implements Handler, Disposable {

    // Modify Listener
    private final PhyModule.ModifyListener modifyListener = new PhyModule.ModifyListener() {
        @Override
        public void add(PhyComponent component) {
            dynamicsWorld.addRigidBody(component.body, component.group, component.mask);
        }
        @Override
        public void remove(PhyComponent component) {
            dynamicsWorld.removeRigidBody(component.body);
        }
    };

    // Tmp Vector
    private static final Vector3 rayFrom = new Vector3();
    private static final Vector3 rayTo = new Vector3();

    // ----- Create DynamicsWorld World ----- //
    protected btDynamicsWorld dynamicsWorld;
    protected DebugDrawer debugDrawer;
    protected ClosestRayResultCallback rayTestCB;
    public PhyHandler() {

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

    // ----- Module ----- //
    private final Map<PhyModule, String> modules = new HashMap<>();
    public void add(Module module, String instanceName) {
        if (!(module instanceof PhyModule)) throw new IllegalArgumentException();
        modules.put((PhyModule) module, instanceName);
        PhyModule phyModule = (PhyModule) module;
        phyModule.setModifyListener(modifyListener);
        for (PhyComponent component : phyModule.getAll()) {
            dynamicsWorld.addRigidBody(component.body, component.group, component.mask);
        }
    }
    public void remove(Module module) {
        if (!(module instanceof PhyModule)) throw new IllegalArgumentException();
        PhyModule phyModule = (PhyModule) module;
        for (PhyComponent component : phyModule.getAll()) {
            dynamicsWorld.removeRigidBody(component.body);
        }
        phyModule.setModifyListener(null);
        modules.remove(module);
    }
    public boolean handle(Module module) {
        return module instanceof PhyModule;
    }
    public PhyModule findModule(PhyComponent component) {
        for (PhyModule module : modules.keySet()) {
            if (module.contain(component)) {
                return module;
            }
        }
        return null;
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
                PhyModule module = findModule((PhyComponent) obj.userData);
                return (module == null) ? null : modules.get(module);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void dispose() {
        for(int i = disposables.size - 1; i >= 0; i--) {
            Disposable disposable = disposables.get(i);
            if(disposable != null)
                disposable.dispose();
        }
    }
    private Array<Disposable> disposables = new Array<>();
    protected void addDisposable(Disposable disposable) {
        disposables.add(disposable);
    }
}
