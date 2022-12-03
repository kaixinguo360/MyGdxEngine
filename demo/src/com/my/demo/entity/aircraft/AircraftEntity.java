package com.my.demo.entity.aircraft;

import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.object.RotateEntity;
import com.my.demo.entity.weapon.BombEntity;
import com.my.demo.entity.weapon.BulletEntity;
import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.enhanced.physics.HingeConstraintController;
import com.my.world.module.common.Position;
import com.my.world.module.particle.ParticlesEffect;
import com.my.world.module.particle.ParticlesSystem;
import com.my.world.module.physics.constraint.HingeConstraint;

public class AircraftEntity extends EnhancedEntity {

    public static String effectPath = "effect/smoke.pfx";
    public static ParticleEffect particleEffect;

    public static void init(Engine engine, Scene scene) {
        AssetsManager assetsManager = engine.getAssetsManager();
        particleEffect = assetsManager.addAsset("smokeEffect", ParticleEffect.class, ParticlesSystem.loadParticleEffect(effectPath));
    }

    public final AircraftScript aircraftScript;
    public final BodyEntity body;
    public final EngineEntity engine;
    public final RotateEntity<HingeConstraintController> rotate_L;
    public final WingEntity wing_L1;
    public final WingEntity wing_L2;
    public final ParticlesEffect smoke_L;
    public final RotateEntity<HingeConstraintController> rotate_R;
    public final WingEntity wing_R1;
    public final WingEntity wing_R2;
    public final ParticlesEffect smoke_R;
    public final RotateEntity<HingeConstraintController> rotate_T;
    public final WingEntity wing_TL;
    public final WingEntity wing_TR;
    public final WingEntity wing_VL;
    public final WingEntity wing_VR;

    public AircraftEntity() {

        // Aircraft Entity
        setName("Aircraft");
        aircraftScript = addComponent(new AircraftScript());
        aircraftScript.bulletBuilder = BulletEntity.builder;
        aircraftScript.bombBuilder = BombEntity.builder;

        // Body
        body = new BodyEntity();
        body.setName("body");
        body.setParent(this);
        body.transform.idt().translate(0, 0.5f, -3);
        addEntity(body);

        engine = new EngineEntity(body);
        engine.setName("engine");
        engine.setParent(this);
        engine.transform.idt().translate(0, 0.6f, -6).rotate(Vector3.X, -90);
        addEntity(engine);

        // Left
        Matrix4 transform_L = new Matrix4().translate(-1, 0.5f, -5).rotate(Vector3.Z, 90);
        rotate_L = new RotateEntity<>(body, new HingeConstraintController(-0.15f, 0.2f, 0.5f));
        rotate_L.setName("rotate_L");
        rotate_L.setParent(this);
        rotate_L.position.setLocalTransform(transform_L);
        rotate_L.constraint.frameInA.set(body.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform_L).rotate(Vector3.X, 90));
        addEntity(rotate_L);

        wing_L1 = new WingEntity(rotate_L);
        wing_L1.setName("wing_L1");
        wing_L1.setParent(this);
        wing_L1.transform.idt().translate(-2.5f, 0.5f, -5).rotate(Vector3.X, 14);
        addEntity(wing_L1);

        wing_L2 = new WingEntity(wing_L1);
        wing_L2.setName("wing_L2");
        wing_L2.setParent(this);
        wing_L2.transform.idt().translate(-4.5f, 0.5f, -5).rotate(Vector3.X, 14);
        smoke_L = wing_L2.addComponent(new ParticlesEffect());
        smoke_L.effect = particleEffect;
        smoke_L.transform = new Matrix4().translate(-0.5f, 0, 0).rotate(Vector3.X, 90);
        addEntity(wing_L2);

        // Right
        Matrix4 transform_R = new Matrix4().translate(1, 0.5f, -5).rotate(Vector3.Z, 90);
        rotate_R = new RotateEntity<>(body, new HingeConstraintController(-0.15f, 0.2f, 0.5f));
        rotate_R.setName("rotate_R");
        rotate_R.setParent(this);
        rotate_R.position.setLocalTransform(transform_R);
        rotate_R.constraint.frameInA.set(body.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform_R).rotate(Vector3.X, 90));
        addEntity(rotate_R);

        wing_R1 = new WingEntity(rotate_R);
        wing_R1.setName("wing_R1");
        wing_R1.setParent(this);
        wing_R1.transform.idt().translate(2.5f, 0.5f, -5).rotate(Vector3.X, 14);
        addEntity(wing_R1);

        wing_R2 = new WingEntity(wing_R1);
        wing_R2.setName("wing_R2");
        wing_R2.setParent(this);
        wing_R2.transform.idt().translate(4.5f, 0.5f, -5).rotate(Vector3.X, 14);
        smoke_R = wing_R2.addComponent(new ParticlesEffect());
        smoke_R.effect = particleEffect;
        smoke_R.transform = new Matrix4().translate(0.5f, 0, 0).rotate(Vector3.X, 90);
        addEntity(wing_R2);

        // Horizontal Tail
        Matrix4 transform_T = new Matrix4().translate(0, 0.5f, 0.1f).rotate(Vector3.Z, 90);
        rotate_T = new RotateEntity<>(body, new HingeConstraintController(-0.2f, 0.2f, 1f));
        rotate_T.setName("rotate_T");
        rotate_T.setParent(this);
        rotate_T.position.setLocalTransform(transform_T);
        rotate_T.constraint.frameInA.set(body.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform_T).rotate(Vector3.X, 90));
        addEntity(rotate_T);

        wing_TL = new WingEntity(rotate_T);
        wing_TL.setName("wing_TL");
        wing_TL.setParent(this);
        wing_TL.transform.idt().translate(-1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f);
        addEntity(wing_TL);

        wing_TR = new WingEntity(rotate_T);
        wing_TR.setName("wing_TR");
        wing_TR.setParent(this);
        wing_TR.transform.idt().translate(1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f);
        addEntity(wing_TR);

        // Vertical Tail
        Matrix4 transform_VL = new Matrix4().translate(-0.6f, 1f, -1).rotate(Vector3.Z, 90);
        wing_VL = new WingEntity();
        wing_VL.setName("wing_VL");
        wing_VL.setParent(this);
        wing_VL.transform.set(transform_VL);
        wing_VL.addComponent(new HingeConstraint(
                body,
                body.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform_VL).translate(0, -0.1f, -0.5f).rotate(Vector3.Y, 90),
                new Matrix4().translate(0, -0.1f, -0.5f).rotate(Vector3.Y, 90),
                false
        ));
        wing_VL.addComponent(new HingeConstraintController(0, 0.2f, 1f));
        addEntity(wing_VL);

        Matrix4 transform_VR = new Matrix4().translate(0.6f, 1f, -1).rotate(Vector3.Z, 90);
        wing_VR = new WingEntity();
        wing_VR.setName("wing_VR");
        wing_VR.setParent(this);
        wing_VR.transform.set(transform_VR);
        wing_VR.addComponent(new HingeConstraint(
                body,
                body.getComponent(Position.class).getGlobalTransform(new Matrix4()).inv().mul(transform_VR).translate(0, 0.1f, -0.5f).rotate(Vector3.Y, 90),
                new Matrix4().translate(0, 0.1f, -0.5f).rotate(Vector3.Y, 90),
                false
        ));
        wing_VR.addComponent(new HingeConstraintController(-0.2f, 0, 1f));
        addEntity(wing_VR);
    }
}
