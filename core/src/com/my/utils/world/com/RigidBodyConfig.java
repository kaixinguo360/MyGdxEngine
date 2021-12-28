package com.my.utils.world.com;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.utils.world.Config;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RigidBodyConfig {

    private static final Vector3 localInertia = new Vector3();

    public btCollisionShape shape;

    @Config
    public float mass;

    public btRigidBody.btRigidBodyConstructionInfo constructionInfo;

    public RigidBodyConfig(btCollisionShape shape, float mass) {
        this.shape = shape;
        this.mass = mass;
    }

    public void generateRigidBodyConfig() {
        if (mass > 0f) {
            shape.calculateLocalInertia(mass, localInertia);
        } else {
            localInertia.set(0, 0, 0);
        }
        this.constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
    }
}
