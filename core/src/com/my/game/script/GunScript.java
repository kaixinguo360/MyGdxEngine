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

public class GunScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate, KeyInputSystem.OnKeyDown {

    private World world;
    private PhysicsSystem physicsSystem;
    private Camera camera;

    private Entity barrel;
    private Entity rotate_Y;
    private Entity rotate_X;

    private GunController gunController_Y;
    private GunController gunController_X;

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
    public Vector3 bombVelocity = new Vector3(0, 0, -100);

    private final static Vector3 bombOffset = new Vector3(0, 0, -5);

    @Override
    public void start(World world, Entity entity) {
        this.world = world;
        this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);

        barrel = entity.findChildByName("barrel");
        rotate_Y = entity.findChildByName("rotate_Y");
        rotate_X = entity.findChildByName("rotate_X");

        this.camera = barrel.getComponent(Camera.class);
        if (rotate_Y.contains(GunController.class)) gunController_Y = rotate_Y.getComponent(GunController.class);
        if (rotate_X.contains(GunController.class)) gunController_X = rotate_X.getComponent(GunController.class);
    }

    @Override
    public void update(World world, Entity entity) {
        if (camera != null && !disabled) {
            float v = 0.025f;
            if (gunController_Y != null && gunController_X != null) {
                if (Gdx.input.isKeyPressed(Input.Keys.W)) rotate(0, -v);
                if (Gdx.input.isKeyPressed(Input.Keys.S)) rotate(0, v);
                if (Gdx.input.isKeyPressed(Input.Keys.A)) rotate(v, 0);
                if (Gdx.input.isKeyPressed(Input.Keys.D)) rotate(-v, 0);
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

    public void update() {
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
        rotate_Y.removeComponent(Constraint.class);
        rotate_X.removeComponent(Constraint.class);
        rotate_Y.removeComponent(ConstraintController.class);
        rotate_X.removeComponent(ConstraintController.class);
        barrel.removeComponent(Constraint.class);
        Vector3 tmpV = Vector3Pool.obtain();
        physicsSystem.addExplosion(getTransform().getTranslation(tmpV), 2000);
        Vector3Pool.free(tmpV);
    }

    public void rotate(float stepY, float stepX) {
        setDirection(gunController_Y.target + stepY, gunController_X.target + stepX);
    }

    public void setDirection(float angleY, float angleX) {
        getBody().activate();
        gunController_Y.target = angleY;
        gunController_X.target = angleX;
    }

    public Matrix4 getTransform() {
        return barrel.getComponent(Position.class).getLocalTransform();
    }

    public btRigidBody getBody() {
        return barrel.getComponent(RigidBody.class).body;
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
