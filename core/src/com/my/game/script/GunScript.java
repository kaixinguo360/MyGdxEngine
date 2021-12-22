package com.my.game.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.game.MyInstance;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Config;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.*;
import com.my.utils.world.sys.CameraSystem;
import com.my.utils.world.sys.KeyInputSystem;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.ScriptSystem;

public class GunScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate, KeyInputSystem.OnKeyDown {

    // ----- Constants ----- //
    private final static short BOMB_FLAG = 1 << 8;
    private final static short GUN_FLAG = 1 << 9;
    private final static short ALL_FLAG = -1;

    // ----- Temporary ----- //
    private static final Vector3 tmpV = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();
    private static final Quaternion tmpQ = new Quaternion();

    private World world;
    private AssetsManager assetsManager;
    private PhysicsSystem physicsSystem;
    private Camera camera;

    private Entity barrel;
    private Entity rotate_Y;
    private Entity rotate_X;

    private GunController gunController_Y;
    private GunController gunController_X;

    @Config
    public boolean disabled;

    @Override
    public void start(World world, Entity entity) {
        this.world = world;
        this.assetsManager = world.getAssetsManager();
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
        float v = 0.025f;
        if (gunController_Y != null && gunController_X != null) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) rotate(0, -v);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) rotate(0, v);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) rotate(v, 0);
            if (Gdx.input.isKeyPressed(Input.Keys.D)) rotate(-v, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.J)) fire();
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) explode();
    }

    public void fire() {
        tmpM.set(getTransform()).translate(0, 0, -20 + (float) (Math.random() * 15)).rotate(Vector3.X, 90);
        getTransform().getRotation(tmpQ);
        tmpV.set(getBody().getLinearVelocity());
        tmpV.add(new Vector3(0, 0, -1).mul(tmpQ).scl(2000));
        btRigidBody body = createBullet(tmpM).getComponent(RigidBody.class).body;
        body.setLinearVelocity(tmpV);
        body.setCcdMotionThreshold(1e-7f);
        body.setCcdSweptSphereRadius(2);
    }

    public void explode() {
        System.out.println("Explosion!");
        rotate_Y.removeComponent(Constraint.class);
        rotate_X.removeComponent(Constraint.class);
        rotate_Y.removeComponent(ConstraintController.class);
        rotate_X.removeComponent(ConstraintController.class);
        barrel.removeComponent(Constraint.class);
        physicsSystem.addExplosion(getTransform().getTranslation(tmpV), 2000);
    }

    private Entity createBullet(Matrix4 transform) {
        Entity entity = new MyInstance(assetsManager, "bullet", null,
                new Collision(BOMB_FLAG, ALL_FLAG));
        entity.setName("Bullet");
        world.getEntityManager().addEntity(entity).getComponent(Position.class).transform.set(transform);
        entity.addComponent(new RemoveScript());
        entity.addComponent(new GunBulletCollisionHandler());
        return entity;
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
        return barrel.getComponent(Position.class).transform;
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
