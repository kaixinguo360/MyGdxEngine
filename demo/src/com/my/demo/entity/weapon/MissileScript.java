package com.my.demo.entity.weapon;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.my.world.core.*;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.gdx.QuaternionPool;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.script.ScriptSystem;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MissileScript implements ScriptSystem.OnStart, PhysicsSystem.OnFixedUpdate {

    @Config
    public final Vector3 direction = new Vector3(0, 1, 0);

    @Config
    public float linearVelocity = 10;

    @Config
    public float angleVelocity = 360;

    @Config
    public final Vector3 targetPosition = new Vector3();

    @Config
    public String targetEntity;

    @Config
    public float leftWorkTime = 5;

    @Config
    public float leftLiveTime = 7;

    protected Entity entity;
    protected TimeManager timeManager;
    protected RigidBody rigidBody;

    @Override
    public void start(Scene scene, Entity entity) {
        this.entity = entity;
        this.timeManager = scene.getTimeManager();
        this.rigidBody = entity.getComponent(RigidBody.class);
        if (rigidBody == null) throw new RuntimeException("No rigidBody component in this entity: id=" + entity.getId());
    }

    @Override
    public void fixedUpdate(Scene scene, btDynamicsWorld dynamicsWorld, Entity entity) {
        if (leftLiveTime <= 0) return;
        leftLiveTime -= timeManager.getDeltaTime();
        if (leftLiveTime <= 0) {
            BombScript bombScript = entity.getComponent(BombScript.class);
            if (bombScript != null) {
                bombScript.explosion();
            }
        }
        if (leftWorkTime <= 0) return;
        leftWorkTime -= timeManager.getDeltaTime();

        Quaternion tmpQ1 = QuaternionPool.obtain();
        Quaternion tmpQ = QuaternionPool.obtain();
        Vector3 tmpV1 = Vector3Pool.obtain();
        Vector3 tmpV2 = Vector3Pool.obtain();
        Vector3 tmpV3 = Vector3Pool.obtain();
        Vector3 tmpV = Vector3Pool.obtain();

        updateTargetPosition(scene);
        Matrix4 currentTransform = rigidBody.body.getWorldTransform();
        Vector3 currentPosition = currentTransform.getTranslation(tmpV1);
        Vector3 currentScale = currentTransform.getScale(tmpV2);
        Quaternion currentRotation = currentTransform.getRotation(tmpQ1);
        Vector3 relativePosition = tmpV3.set(targetPosition).sub(currentPosition);

        float relativeAngle = angle(direction, relativePosition);
        if (relativeAngle != 0) {
            Quaternion targetRotation = tmpQ.set(tmpV.set(direction).crs(relativePosition), relativeAngle);
            float percentage = MathUtils.clamp((angleVelocity * timeManager.getDeltaTime()) / relativeAngle, 0, 1);
            currentRotation.slerp(targetRotation, percentage);
            this.rigidBody.body.setWorldTransform(currentTransform.set(currentPosition, currentRotation, currentScale));
        }

        this.rigidBody.body.setLinearVelocity(tmpV.set(direction).nor().scl(linearVelocity).mul(currentRotation));

        Vector3Pool.free(tmpV1);
        Vector3Pool.free(tmpV2);
        Vector3Pool.free(tmpV3);
        Vector3Pool.free(tmpV);
        QuaternionPool.free(tmpQ1);
        QuaternionPool.free(tmpQ);
    }

    protected void updateTargetPosition(Scene scene) {
        if (targetEntity != null) {
            Entity entity;
            try {
                entity = scene.getEntityManager().findEntityById(targetEntity);
            } catch (EntityManager.EntityManagerException e) {
                return;
            }
            Position position = entity.getComponent(Position.class);
            Matrix4 tmpM = Matrix4Pool.obtain();
            position.getGlobalTransform(tmpM).getTranslation(targetPosition);
            Matrix4Pool.free(tmpM);
        }
    }

    public static float angle(Vector3 v1, Vector3 v2) {
        float vDot = v1.dot(v2) / (v1.len() * v2.len());
        if (vDot < -1.0) vDot = -1.0f;
        if (vDot > 1.0) vDot = 1.0f;
        return (float) Math.toDegrees(Math.acos(vDot));
    }
}
