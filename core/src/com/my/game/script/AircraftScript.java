package com.my.game.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.game.MyInstance;
import com.my.utils.world.*;
import com.my.utils.world.com.*;
import com.my.utils.world.sys.CameraSystem;
import com.my.utils.world.sys.KeyInputSystem;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.ScriptSystem;

import java.lang.System;

public class AircraftScript implements Loadable.OnInit, ScriptSystem.OnStart, ScriptSystem.OnUpdate, KeyInputSystem.OnKeyDown {

    // ----- Constants ----- //
    private final static short BOMB_FLAG = 1 << 8;
    private final static short AIRCRAFT_FLAG = 1 << 9;
    private final static short ALL_FLAG = -1;

    // ----- Temporary ----- //
    private static final Vector3 tmpV1 = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();
    private static final Quaternion tmpQ = new Quaternion();

    private World world;
    private AssetsManager assetsManager;
    private PhysicsSystem physicsSystem;
    private Camera camera;

    @Config public Entity body;
    @Config public Entity engine;
    @Config public Entity rotate_L, rotate_R, rotate_T;
    @Config public Entity wing_L1, wing_L2;
    @Config public Entity wing_R1, wing_R2;
    @Config public Entity wing_TL, wing_TR;
    @Config public Entity wing_VL, wing_VR;

    @Config public boolean disabled;
    @Config public int bombNum;

    public AircraftController aircraftController_L;
    public AircraftController aircraftController_R;
    public AircraftController aircraftController_T;

    @Override
    public void init() {
        if (rotate_L.contains(AircraftController.class))
            aircraftController_L = rotate_L.getComponent(AircraftController.class);
        if (rotate_R.contains(AircraftController.class))
            aircraftController_R = rotate_R.getComponent(AircraftController.class);
        if (rotate_T.contains(AircraftController.class))
            aircraftController_T = rotate_T.getComponent(AircraftController.class);
    }

    @Override
    public void start(World world, Entity entity) {
        this.world = world;
        this.assetsManager = world.getAssetsManager();
        this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
        this.camera = body.getComponent(Camera.class);
    }

    @Override
    public void update(World world, Entity entity) {
        if (camera == null || disabled) return;
        update();
    }

    @Override
    public void keyDown(World world, Entity entity, int keycode) {
        if (camera == null) return;
        if (keycode == Input.Keys.TAB) changeCamera();
        if (keycode == Input.Keys.SHIFT_LEFT && !disabled) changeCameraFollowType();
    }

    public void update() {
        float v1 = 1f;
        float v2 = 0.5f;
        if (aircraftController_L != null && aircraftController_R != null) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) aircraftController_T.rotate(v1);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) aircraftController_T.rotate(-v1);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                aircraftController_L.rotate(v2);
                aircraftController_R.rotate(-v2);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                aircraftController_L.rotate(-v2);
                aircraftController_R.rotate(v2);
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.J)) fire();
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) explode();
    }

    public void fire() {
        tmpM.set(getTransform()).translate(0, 0, -20 + (float) (Math.random() * 15)).rotate(Vector3.X, 90);
        getTransform().getRotation(tmpQ);
        tmpV1.set(getBody().getLinearVelocity());
        tmpV1.add(new Vector3(0, 0, -1).mul(tmpQ).scl(2000));
        btRigidBody body = createBomb(tmpM).getComponent(RigidBody.class).body;
        body.setLinearVelocity(tmpV1);
        body.setCcdMotionThreshold(1e-7f);
        body.setCcdSweptSphereRadius(2);
    }

    public void explode() {
        System.out.println("Explosion!");
        body.removeComponent(Constraint.class);
        engine.removeComponent(Constraint.class);
        rotate_L.removeComponent(Constraint.class);
        rotate_R.removeComponent(Constraint.class);
        rotate_T.removeComponent(Constraint.class);
        rotate_L.removeComponent(ConstraintController.class);
        rotate_R.removeComponent(ConstraintController.class);
        rotate_T.removeComponent(ConstraintController.class);
        wing_L1.removeComponent(Constraint.class);
        wing_L2.removeComponent(Constraint.class);
        wing_R1.removeComponent(Constraint.class);
        wing_R2.removeComponent(Constraint.class);
        wing_TL.removeComponent(Constraint.class);
        wing_TR.removeComponent(Constraint.class);
        wing_VL.removeComponent(Constraint.class);
        wing_VR.removeComponent(Constraint.class);
        physicsSystem.addExplosion(getTransform().getTranslation(tmpV1), 2000);
    }

    public float getVelocity() {
        return getBody().getLinearVelocity().len();
    }

    public float getHeight() {
        return getTransform().getTranslation(tmpV1).y;
    }

    public Matrix4 getTransform() {
        return body.getComponent(Position.class).transform;
    }

    public btRigidBody getBody() {
        return body.getComponent(RigidBody.class).body;
    }

    public Entity createBomb(Matrix4 transform) {
        Entity entity = new MyInstance(assetsManager, "bomb", null,
                new Collision(BOMB_FLAG, ALL_FLAG));
        entity.setId("Bomb-" + bombNum++);
        world.getEntityManager().addEntity(entity).getComponent(Position.class).transform.set(transform);
        entity.addComponent(new RemoveScript());
        entity.addComponent(new AircraftBombCollisionHandler());
        return entity;
    }

    public void changeCamera() {
        disabled = !disabled;
        if (!disabled) {
            camera.layer = 0;
            camera.startX = 0;
            camera.startY = 0;
            camera.endX = 1;
            camera.endY = 1;
        } else {
            camera.layer = 1;
            camera.startX = 0;
            camera.startY = 0.7f;
            camera.endX = 0.3f;
            camera.endY = 1;
        }
        world.getSystemManager().getSystem(CameraSystem.class).updateCameras();
    }

    public void changeCameraFollowType() {
        switch (camera.followType) {
            case A:
                camera.followType = CameraSystem.FollowType.B;
                break;
            case B:
                camera.followType = CameraSystem.FollowType.A;
                break;
        }
    }
}
