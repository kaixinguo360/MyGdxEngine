package com.my.world.module.physics.script;

import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.script.ScriptSystem;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class CollisionHandler extends ActivatableComponent implements ScriptSystem.OnStart, PhysicsSystem.OnCollision {

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
