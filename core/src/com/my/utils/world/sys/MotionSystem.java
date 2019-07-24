package com.my.utils.world.sys;

import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.com.Motion;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;

public class MotionSystem extends BaseSystem {

    // ----- Check ----- //
    public boolean check(Entity entity) {
        return entity.contain(Position.class, RigidBody.class, Motion.class);
    }

    // ----- Custom ----- //
    public void update() {
        for (Entity entity : entities) {
            entity.get(Motion.class).update(entity.get(RigidBody.class).body, entity.get(Position.class));
        }
    }

}
