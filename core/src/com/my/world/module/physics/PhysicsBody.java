package com.my.world.module.physics;

import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.my.world.core.Config;
import com.my.world.module.common.BaseActivatableComponent;
import com.my.world.module.common.Position;

public abstract class PhysicsBody extends BaseActivatableComponent {

    // ----- Static ----- //
    public final static short STATIC_FLAG = 1 << 8;
    public final static short NORMAL_FLAG = 1 << 9;
    public final static short ALL_FLAG = -1;

    @Config
    public int group = NORMAL_FLAG;

    @Config
    public int mask = ALL_FLAG;

    @Config
    public boolean isTrigger;

    public Position position;
    public PhysicsSystem physicsSystem;
    public btDynamicsWorld dynamicsWorld;

    public void addToWorld(btDynamicsWorld dynamicsWorld) {
        this.position = entity.getComponent(Position.class);
        this.dynamicsWorld = dynamicsWorld;
    }

    public void removeFromWorld(btDynamicsWorld dynamicsWorld) {
        this.position = null;
        this.dynamicsWorld = null;
    }

    public boolean isAddedToWorld() {
        return dynamicsWorld != null;
    }

    public void reset() {
        if (!isAddedToWorld()) throw new RuntimeException("This component not attached to a PhysicsSystem: " + this);
        btDynamicsWorld currentWorld = dynamicsWorld;
        removeFromWorld(currentWorld);
        addToWorld(currentWorld);
    };

    public abstract void syncTransformFromEntity();

    public abstract void syncTransformFromWorld();
}
