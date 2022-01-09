package com.my.world.module.physics.force;

import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.script.ScriptSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public abstract class Force implements ScriptSystem.OnStart, PhysicsSystem.OnFixedUpdate {

    protected Entity entity;
    protected RigidBody rigidBody;
    protected Position position;

    @Override
    public void start(Scene scene, Entity entity) {
        if (!entity.contain(RigidBody.class)) throw new RuntimeException("Required component not found: RigidBody");
        if (!entity.contain(Position.class)) throw new RuntimeException("Required component not found: Position");
        this.entity = entity;
        this.rigidBody = entity.getComponent(RigidBody.class);
        this.position = entity.getComponent(Position.class);
    }

    @Override
    public void fixedUpdate(Scene scene, btDynamicsWorld dynamicsWorld, Entity entity) {
        this.update();
    }

    public abstract void update();
}
