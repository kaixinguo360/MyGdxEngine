package com.my.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.com.*;
import com.my.utils.world.sys.RenderSystem;

public class MyInstance extends Entity implements Disposable {

    protected Position position;
    protected Render render;
    protected RigidBody rigidBody;

    public MyInstance() {}

    public MyInstance(AssetsManager assetsManager, String className) {
        this(assetsManager, className, null);
    }

    public MyInstance(AssetsManager assetsManager, String className, Motion motion) {
        this(assetsManager, className, motion, null);
    }

    public MyInstance(AssetsManager assetsManager, String className, Motion motion, Collision collision) {
        position = addComponent(new Position(new Matrix4()));
        if (assetsManager.hasAsset(className, RenderSystem.RenderModel.class)) {
            RenderSystem.RenderModel renderModel = assetsManager.getAsset(className, RenderSystem.RenderModel.class);
            render = addComponent(new Render(renderModel));
        }
        if (assetsManager.hasAsset(className, btRigidBody.btRigidBodyConstructionInfo.class)) {
            btRigidBody.btRigidBodyConstructionInfo rigidBodyConfig = assetsManager.getAsset(className, btRigidBody.btRigidBodyConstructionInfo.class);
            rigidBody = addComponent(new RigidBody(rigidBodyConfig));
        }
        if (motion != null) {
            addComponent(motion);
        }
        if (collision != null) {
            addComponent(collision);
        }
    }

    public void setTransform(Matrix4 transform) {
        getComponent(Position.class).setLocalTransform(transform);
        if (contain(RigidBody.class)) getComponent(RigidBody.class).body.proceedToTransform(transform);
    }

    @Override
    public void dispose() {
        if (contain(RigidBody.class)) getComponent(RigidBody.class).body.dispose();
    }
}
