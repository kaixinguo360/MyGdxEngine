package com.my.utils.world.com;

import com.my.utils.world.*;
import com.my.utils.world.sys.PhysicsSystem;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Collision implements Component, StandaloneResource {

    @Config
    public int callbackFlag;

    @Config
    public int callbackFilter;

    protected World world;
    protected PhysicsSystem physicsSystem;
    protected Entity self;

    public Collision(int callbackFlag, int callbackFilter) {
        this.callbackFlag = callbackFlag;
        this.callbackFilter = callbackFilter;
    }

    public void init(World world, Entity entity) {
        this.world = world;
        this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
        this.self = entity;
    }

    public abstract void handle(Entity target);
}
