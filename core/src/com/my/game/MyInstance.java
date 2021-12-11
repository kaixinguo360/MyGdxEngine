package com.my.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.com.*;
import com.my.utils.world.sys.PhysicsSystem;

public class MyInstance extends Entity implements Disposable {

    public static AssetsManager assetsManager;

    protected Position position;
    protected Render render;
    protected RigidBody rigidBody;
    protected Serialization serialization;

    public MyInstance() {}

    public MyInstance(String className) {
        this(className, null);
    }

    public MyInstance(String className, String group) {
        this(className, group, null);
    }

    public MyInstance(String className, String group, Motion motion) {
        this(className, group, motion, null);
    }

    public MyInstance(String className, String group, Motion motion, Collision collision) {
        position = addComponent(new Position(new Matrix4()));
        render = addComponent(Render.get(className, position));
        if (assetsManager.hasAsset(className, PhysicsSystem.RigidBodyConfig.class)) {
            PhysicsSystem.RigidBodyConfig rigidBodyConfig = assetsManager.getAsset(className, PhysicsSystem.RigidBodyConfig.class);
            rigidBody = addComponent(rigidBodyConfig.newInstance());
        }
        if (group != null) {
            serialization = new Serialization();
            serialization.group = group;
            serialization.serializerId = className;
            addComponent(serialization);
        }
        if (motion != null) {
            addComponent(motion);
        }
        if (collision != null) {
            addComponent(collision);
        }
    }

    public void setTransform(Matrix4 transform) {
        getComponent(Position.class).transform.set(transform);
        if (contain(RigidBody.class)) getComponent(RigidBody.class).body.proceedToTransform(transform);
    }

    @Override
    public void dispose() {
        if (contain(RigidBody.class)) getComponent(RigidBody.class).body.dispose();
    }
}
