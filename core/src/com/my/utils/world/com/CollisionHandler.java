package com.my.utils.world.com;

import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.ScriptSystem;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class CollisionHandler implements ScriptSystem.OnStart, PhysicsSystem.OnCollision {

    protected World world;
    protected PhysicsSystem physicsSystem;
    protected Entity self;

    @Override
    public void start(World world, Entity entity) {
        this.world = world;
        this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
        this.self = entity;
    }
}
