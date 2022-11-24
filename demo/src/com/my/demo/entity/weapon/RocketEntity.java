package com.my.demo.entity.weapon;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.gdx.Vector3Pool;

import java.util.function.Consumer;

public class RocketEntity extends BombEntity {

    public final RocketScript rocketScript;

    public static void init(Engine engine, Scene scene) {}

    public RocketEntity() {
        setName("Rocket");
        rocketScript = addComponent(new RocketScript());
    }

    public static RocketEntity createRocket(Consumer<Matrix4> consumer, String targetId, float linearVelocity, float angleVelocity, Vector3 impulse) {
        Vector3 tmpV = Vector3Pool.obtain();
        RocketEntity rocket = new RocketEntity();
        rocket.position.setGlobalTransform(consumer);
        rocket.rocketScript.targetEntity = targetId;
        rocket.rocketScript.linearVelocity = linearVelocity;
        rocket.rocketScript.angleVelocity = angleVelocity;
        rocket.rigidBody.body.applyImpulse(tmpV.set(impulse).rot(rocket.position.getGlobalTransform()), Vector3.Zero);
        Vector3Pool.free(tmpV);
        return rocket;
    }
}
