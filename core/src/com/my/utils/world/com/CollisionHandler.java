package com.my.utils.world.com;

import com.my.utils.world.Entity;
import com.my.utils.world.Scene;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.ScriptSystem;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class CollisionHandler implements ScriptSystem.OnStart, PhysicsSystem.OnCollision {

    protected Scene scene;
    protected PhysicsSystem physicsSystem;
    protected Entity self;

    @Override
    public void start(Scene scene, Entity entity) {
        this.scene = scene;
        this.physicsSystem = scene.getSystemManager().getSystem(PhysicsSystem.class);
        this.self = entity;
    }
}
