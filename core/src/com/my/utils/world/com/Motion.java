package com.my.utils.world.com;

import com.my.utils.world.Entity;
import com.my.utils.world.World;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public abstract class Motion extends Script implements Script.OnInit, Script.OnUpdate {

    protected Entity entity;
    protected RigidBody rigidBody;
    protected Position position;

    @Override
    public void init(World world, Entity entity) {
        if (!entity.contain(RigidBody.class)) throw new RuntimeException("Required component not found: RigidBody");
        if (!entity.contain(Position.class)) throw new RuntimeException("Required component not found: Position");
        this.entity = entity;
        this.rigidBody = entity.getComponent(RigidBody.class);
        this.position = entity.getComponent(Position.class);
    }

    @Override
    public void update(World world, Entity entity) {
        this.update();
    }

    public abstract void update();
}
