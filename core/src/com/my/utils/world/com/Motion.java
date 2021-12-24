package com.my.utils.world.com;

import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.ScriptSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public abstract class Motion implements ScriptSystem.OnStart, PhysicsSystem.OnFixedUpdate {

    protected Entity entity;
    protected RigidBody rigidBody;
    protected Position position;

    @Override
    public void start(World world, Entity entity) {
        if (!entity.contain(RigidBody.class)) throw new RuntimeException("Required component not found: RigidBody");
        if (!entity.contain(Position.class)) throw new RuntimeException("Required component not found: Position");
        this.entity = entity;
        this.rigidBody = entity.getComponent(RigidBody.class);
        this.position = entity.getComponent(Position.class);
    }

    @Override
    public void fixedUpdate(World world, btDynamicsWorld dynamicsWorld, Entity entity) {
        this.update();
    }

    public abstract void update();
}
