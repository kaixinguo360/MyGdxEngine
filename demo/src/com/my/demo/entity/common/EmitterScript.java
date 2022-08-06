package com.my.demo.entity.common;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.EntityBuilder;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.gdx.QuaternionPool;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.camera.Camera;
import com.my.world.module.camera.script.EnhancedThirdPersonCameraController;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.common.Position;
import com.my.world.module.physics.Constraint;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.physics.script.ConstraintController;
import com.my.world.module.script.ScriptSystem;

import java.util.ArrayList;
import java.util.List;

public class EmitterScript extends ActivatableComponent implements ScriptSystem.OnStart {

    @Config
    public String name;

    protected Scene scene;
    protected PhysicsSystem physicsSystem;

    protected Entity main;
    protected Camera camera;
    protected EnhancedThirdPersonCameraController cameraController;

    protected List<Entity> parts = new ArrayList<>();

    @Override
    public void start(Scene scene, Entity entity) {
        this.scene = scene;
        this.physicsSystem = scene.getSystemManager().getSystem(PhysicsSystem.class);
    }

    public void fire(EntityBuilder builder, Vector3 velocity, Vector3 position, float random) {
        Vector3 tmpV1 = Vector3Pool.obtain();
        Vector3 tmpV2 = Vector3Pool.obtain();
        Vector3 tmpV3 = Vector3Pool.obtain();
        Matrix4 tmpM = Matrix4Pool.obtain();
        Quaternion tmpQ = QuaternionPool.obtain();

        tmpV3.set(position).scl(random);
        tmpM.set(getTransform()).translate(position).translate(tmpV3).rotate(Vector3.X, -90);
        getTransform().getRotation(tmpQ);
        tmpV1.set(getMainBody().getLinearVelocity());
        tmpV1.add(tmpV2.set(velocity).mul(tmpQ));

        Entity entity = builder.build(scene);
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
        return main.getComponent(Position.class).getGlobalTransform();
    }

    public btRigidBody getMainBody() {
        return main.getComponent(RigidBody.class).body;
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
