package com.my.game.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.game.LoadUtil;
import com.my.utils.world.Config;
import com.my.utils.world.Entity;
import com.my.utils.world.Prefab;
import com.my.utils.world.World;
import com.my.utils.world.com.*;
import com.my.utils.world.sys.CameraSystem;
import com.my.utils.world.sys.KeyInputSystem;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.ScriptSystem;
import com.my.utils.world.util.pool.Matrix4Pool;
import com.my.utils.world.util.pool.QuaternionPool;
import com.my.utils.world.util.pool.Vector3Pool;

public class AircraftScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate, KeyInputSystem.OnKeyDown {

    private World world;
    private PhysicsSystem physicsSystem;
    private Camera camera;

    public Entity body;
    public Entity engine;
    public Entity rotate_L, rotate_R, rotate_T;
    public Entity wing_L1, wing_L2;
    public Entity wing_R1, wing_R2;
    public Entity wing_TL, wing_TR;
    public Entity wing_VL, wing_VR;

    private AircraftController aircraftController_L;
    private AircraftController aircraftController_R;
    private AircraftController aircraftController_T;

    @Config
    public boolean disabled;

    @Config(type = Config.Type.Asset)
    public Prefab bulletPrefab;

    @Config
    public Vector3 bulletVelocity = new Vector3(0, 0, -2000);

    private final static Vector3 bulletOffset = new Vector3(0, 0, -5);

    @Config(type = Config.Type.Asset)
    public Prefab bombPrefab;

    @Config
    public Vector3 bombVelocity = new Vector3(0, -10, 0);

    private final static Vector3 bombOffset = new Vector3(0, -2, 0);

    @Override
    public void start(World world, Entity entity) {
        this.world = world;
        this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);

        body = entity.findChildByName("body");
        engine = entity.findChildByName("engine");
        rotate_L = entity.findChildByName("rotate_L");
        rotate_R = entity.findChildByName("rotate_R");
        rotate_T = entity.findChildByName("rotate_T");
        wing_L1 = entity.findChildByName("wing_L1");
        wing_L2 = entity.findChildByName("wing_L2");
        wing_R1 = entity.findChildByName("wing_R1");
        wing_R2 = entity.findChildByName("wing_R2");
        wing_TL = entity.findChildByName("wing_TL");
        wing_TR = entity.findChildByName("wing_TR");
        wing_VL = entity.findChildByName("wing_VL");
        wing_VR = entity.findChildByName("wing_VR");

        this.camera = body.getComponent(Camera.class);
        if (rotate_L.contains(AircraftController.class))
            aircraftController_L = rotate_L.getComponent(AircraftController.class);
        if (rotate_R.contains(AircraftController.class))
            aircraftController_R = rotate_R.getComponent(AircraftController.class);
        if (rotate_T.contains(AircraftController.class))
            aircraftController_T = rotate_T.getComponent(AircraftController.class);
    }

    @Override
    public void update(World world, Entity entity) {
        if (camera != null && !disabled) {
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
            if (Gdx.input.isKeyPressed(Input.Keys.J)) fire(bulletPrefab, bulletVelocity, bulletOffset, (float) Math.random());
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) explode();
        }
    }

    @Override
    public void keyDown(World world, Entity entity, int keycode) {
        if (camera == null) return;
        if (keycode == Input.Keys.TAB) changeCamera();
        if (keycode == Input.Keys.SHIFT_LEFT && !disabled) changeCameraFollowType();
        if (camera != null && !disabled) {
            if (keycode == Input.Keys.K) fire(bombPrefab, bombVelocity, bombOffset, (float) Math.random());
        }
    }

    public void fire(Prefab prefab, Vector3 velocity, Vector3 position, float random) {
        Vector3 tmpV1 = Vector3Pool.obtain();
        Vector3 tmpV2 = Vector3Pool.obtain();
        Vector3 tmpV3 = Vector3Pool.obtain();
        Matrix4 tmpM = Matrix4Pool.obtain();
        Quaternion tmpQ = QuaternionPool.obtain();

        tmpV3.set(position).scl(random);
        tmpM.set(getTransform()).translate(position).translate(tmpV3).rotate(Vector3.X, 90);
        getTransform().getRotation(tmpQ);
        tmpV1.set(getBody().getLinearVelocity());
        tmpV1.add(tmpV2.set(velocity).mul(tmpQ));

        Entity entity = prefab.newInstance(LoadUtil.loaderManager, world);
        entity.getComponent(Position.class).setLocalTransform(tmpM);
        btRigidBody body = entity.getComponent(RigidBody.class).body;

        body.setLinearVelocity(tmpV1);
        body.setCcdMotionThreshold(1e-7f);

        Vector3Pool.free(tmpV1);
        Vector3Pool.free(tmpV2);
        Vector3Pool.free(tmpV3);
        Matrix4Pool.free(tmpM);
        QuaternionPool.free(tmpQ);
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

        Vector3 tmpV = Vector3Pool.obtain();
        physicsSystem.addExplosion(getTransform().getTranslation(tmpV), 2000);
        Vector3Pool.free(tmpV);
    }

    public float getVelocity() {
        return getBody().getLinearVelocity().len();
    }

    public float getHeight() {
        Vector3 tmpV = Vector3Pool.obtain();
        float y = getTransform().getTranslation(tmpV).y;
        Vector3Pool.free(tmpV);
        return y;
    }

    public Matrix4 getTransform() {
        return body.getComponent(Position.class).getLocalTransform();
    }

    public btRigidBody getBody() {
        return body.getComponent(RigidBody.class).body;
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
