package com.my.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.com.*;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;

public class MyInstance extends Entity implements Disposable {

    protected Position position;
    protected Render render;
    protected RigidBody rigidBody;
    protected Serialization serialization;

    public MyInstance(AssetsManager assetsManager, String className) {
        this(assetsManager, className, null);
    }

    public MyInstance(AssetsManager assetsManager, String className, String group) {
        this(assetsManager, className, group, null);
    }

    public MyInstance(AssetsManager assetsManager, String className, String group, Motion motion) {
        this(assetsManager, className, group, motion, null);
    }

    public MyInstance(AssetsManager assetsManager, String className, String group, Motion motion, Collision collision) {
        position = addComponent(new Position(new Matrix4()));
        if (assetsManager.hasAsset(className, RenderSystem.RenderConfig.class)) {
            RenderSystem.RenderConfig renderConfig = assetsManager.getAsset(className, RenderSystem.RenderConfig.class);
            render = addComponent(renderConfig.newInstance());
        }
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
