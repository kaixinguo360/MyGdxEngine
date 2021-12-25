package com.my.game.script;

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
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.ScriptSystem;
import com.my.utils.world.util.pool.Matrix4Pool;
import com.my.utils.world.util.pool.QuaternionPool;
import com.my.utils.world.util.pool.Vector3Pool;

import java.util.ArrayList;
import java.util.List;

public class EmitterScript implements ScriptSystem.OnStart {

    @Config
    public boolean disabled;

    protected World world;
    protected PhysicsSystem physicsSystem;

    protected Entity main;
    protected Camera camera;

    protected List<Entity> parts = new ArrayList<>();

    @Override
    public void start(World world, Entity entity) {
        this.world = world;
        this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
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
        tmpV1.set(getMainBody().getLinearVelocity());
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

        main.removeComponent(Constraint.class);
        main.removeComponent(ConstraintController.class);
        for (Entity part : parts) {
            part.removeComponent(Constraint.class);
            part.removeComponent(ConstraintController.class);
        }

        Vector3 tmpV = Vector3Pool.obtain();
        physicsSystem.addExplosion(getTransform().getTranslation(tmpV), 2000);
        Vector3Pool.free(tmpV);
    }

    public Matrix4 getTransform() {
        return main.getComponent(Position.class).getLocalTransform();
    }

    public btRigidBody getMainBody() {
        return main.getComponent(RigidBody.class).body;
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

    public float getVelocity() {
        return getMainBody().getLinearVelocity().len();
    }

    public float getHeight() {
        Vector3 tmpV = Vector3Pool.obtain();
        float y = getTransform().getTranslation(tmpV).y;
        Vector3Pool.free(tmpV);
        return y;
    }
}
