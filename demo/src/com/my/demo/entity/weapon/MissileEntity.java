package com.my.demo.entity.weapon;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.gdx.Vector3Pool;

import java.util.function.Consumer;

public class MissileEntity extends BombEntity {

    public final MissileScript missileScript;

    public static void init(Engine engine, Scene scene) {}

    public MissileEntity() {
        setName("Missile");
        missileScript = addComponent(new MissileScript());
    }

    public static MissileEntity createMissile(Consumer<Matrix4> consumer, String targetId, float linearVelocity, float angleVelocity, Vector3 impulse) {
        Vector3 tmpV = Vector3Pool.obtain();
        MissileEntity missile = new MissileEntity();
        missile.position.setGlobalTransform(consumer);
        missile.missileScript.targetEntity = targetId;
        missile.missileScript.linearVelocity = linearVelocity;
        missile.missileScript.angleVelocity = angleVelocity;
        missile.rigidBody.body.applyImpulse(tmpV.set(impulse).rot(missile.position.getGlobalTransform()), Vector3.Zero);
        Vector3Pool.free(tmpV);
        return missile;
    }
}
