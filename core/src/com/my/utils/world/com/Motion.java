package com.my.utils.world.com;

import com.my.utils.world.Entity;
import com.my.utils.world.World;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public abstract class Motion extends Script {

    protected RigidBody rigidBody;
    protected Position position;

    @Override
    public void init(World world, Entity entity) {
        if (!entity.contain(RigidBody.class)) throw new RuntimeException("Required component not found: RigidBody");
        if (!entity.contain(Position.class)) throw new RuntimeException("Required component not found: Position");
        rigidBody = entity.getComponent(RigidBody.class);
        position = entity.getComponent(Position.class);
    }

    @Override
    public void execute(World world, Entity entity) {
        this.update();
    }

    public abstract void update();
}
