package com.my.world.module.physics;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.world.core.Config;
import com.my.world.core.Configurable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TemplateRigidBody extends RigidBody implements Configurable.OnInit {

    private static final Vector3 localInertia = new Vector3();

    @Config
    public float mass;

    public btCollisionShape shape;

    public btRigidBody.btRigidBodyConstructionInfo constructionInfo;

    protected TemplateRigidBody(boolean isTrigger) {
        super(isTrigger);
    }

    public TemplateRigidBody(btCollisionShape shape, float mass) {
        this(shape, mass, false);
    }

    public TemplateRigidBody(btCollisionShape shape, float mass, boolean isTrigger) {
        super(isTrigger);
        this.shape = shape;
        this.mass = mass;
        init();
    }

    @Override
    public void init() {
        generateRigidBodyConfig();
        this.body = new btRigidBody(constructionInfo);
    }

    @Override
    public void dispose() {
        super.dispose();
        constructionInfo.dispose();
        shape.dispose();
    }

    protected void generateRigidBodyConfig() {
        if (mass > 0f) {
            shape.calculateLocalInertia(mass, localInertia);
        } else {
            localInertia.set(0, 0, 0);
        }
        this.constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
    }
}
