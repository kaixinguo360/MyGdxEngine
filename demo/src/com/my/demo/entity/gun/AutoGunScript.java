package com.my.demo.entity.gun;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.weapon.MissileEntity;
import com.my.world.core.*;
import com.my.world.enhanced.builder.EntityBuilder;
import com.my.world.enhanced.physics.HingeConstraintController;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.common.Position;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.script.ScriptSystem;

import static com.my.demo.entity.weapon.MissileScript.angle;

public class AutoGunScript extends ActivatableComponent implements ScriptSystem.OnStart, ScriptSystem.OnUpdate {

    @Config public final Vector3 direction = new Vector3(0, 0, -1);
    @Config public final Vector3 targetPosition = new Vector3();
    @Config public String targetEntity;
    @Config public float angleVelocity = 36;
    @Config public float fireAngle = 30;
    @Config public int fireInterval = 5;

    @Config(type = Config.Type.Asset) public EntityBuilder bulletBuilder;
    @Config public final Vector3 bulletVelocity = new Vector3(0, 2000, 0);
    @Config public final Matrix4 bulletOffset = new Matrix4().translate(0, 0, -5).rotate(Vector3.X, -90);

    protected TimeManager timeManager;
    protected Entity barrel;
    protected RigidBody barrelBody;
    protected Entity rotateY;
    protected Entity rotateX;
    protected HingeConstraintController controllerY;
    protected HingeConstraintController controllerX;

    @Override
    public void start(Scene scene, Entity entity) {
        this.timeManager = scene.getTimeManager();
        this.barrel = entity.findChildByName("barrel");
        this.rotateY = entity.findChildByName("rotate_Y");
        this.rotateX = entity.findChildByName("rotate_X");
        this.barrelBody = barrel.getComponent(RigidBody.class);
        this.controllerY = rotateY.getComponent(HingeConstraintController.class);
        this.controllerX = rotateX.getComponent(HingeConstraintController.class);
    }

    @Override
    public void update(Scene scene, Entity entity) {
        updateTargetPosition(scene);
        barrelBody.body.activate();
        Matrix4 currentTransform = entity.getComponent(Position.class).getGlobalTransform();
        Vector3 currentPosition = currentTransform.getTranslation(new Vector3());
        Vector3 targetDirection = new Vector3().set(targetPosition).sub(currentPosition);
        Quaternion targetRotation = new Quaternion().set(new Vector3().set(direction).crs(targetDirection), angle(direction, targetDirection));
        float targetX = -targetRotation.getPitch();
        float targetY = targetRotation.getYaw();
        float currentX = (float) Math.toDegrees(controllerX.current);
        float currentY = (float) Math.toDegrees(controllerY.current);
        float relativeAngleX = (targetX - currentX + 360 + 180) % 360 - 180;
        float relativeAngleY = (targetY - currentY + 360 + 180) % 360 - 180;
        float maxAngle = angleVelocity * timeManager.getDeltaTime();
        float changeAngleX = MathUtils.clamp(relativeAngleX, -maxAngle, maxAngle);
        float changeAngleY = MathUtils.clamp(relativeAngleY, -maxAngle, maxAngle);
        controllerX.current = (float) Math.toRadians((currentX + changeAngleX + 360 + 180) % 360 - 180);
        controllerY.current = (float) Math.toRadians((currentY + changeAngleY + 360 + 180) % 360 - 180);
        if (timeManager.getFrameCount() % fireInterval == 0 && Math.abs(relativeAngleX) < fireAngle / 2 && Math.abs(relativeAngleY) < fireAngle / 2) {
//            Vector3 tmpV = Vector3Pool.obtain();
//            Entity bullet = bulletBuilder.build(scene);
//            Position position = bullet.getComponent(Position.class);
//            position.setGlobalTransform(m -> m.mul(barrel.getComponent(Position.class).getGlobalTransform()).mul(bulletOffset));
//            RigidBody rigidBody = bullet.getComponent(RigidBody.class);
//            rigidBody.body.setLinearVelocity(tmpV.set(bulletVelocity).rot(position.getGlobalTransform()));
//            rigidBody.body.setCcdMotionThreshold(1e-7f);
//            Vector3Pool.free(tmpV);
            if (timeManager.getFrameCount() % 30 == 0) {
                Vector3 impulse = new Vector3(0, 1500, 0);
                MissileEntity.createMissile(m -> {
                    m.mul(barrel.getComponent(Position.class).getGlobalTransform());
                    m.rotate(Vector3.Z, MathUtils.random(-60, 60));
                    m.translate(0, 2, 0);
                    m.mul(bulletOffset);
                    m.rotate(Vector3.X, 40);
                }, targetEntity, 100, 180, impulse).addToScene(scene);
            }
        }
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
}
