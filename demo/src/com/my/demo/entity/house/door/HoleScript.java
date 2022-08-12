package com.my.demo.entity.house.door;

import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.enhanced.physics.SimpleAntiShakeCollisionHandler;
import com.my.world.module.physics.PhysicsBody;

public class HoleScript extends SimpleAntiShakeCollisionHandler {

    @Config
    public int group;

    @Override
    protected void onTouch(Entity entity, OverlappedEntityInfo info) {
        System.out.println("onTouch: " + entity.getId());
        PhysicsBody physicsBody = entity.getComponent(PhysicsBody.class);
        if (physicsBody != null && physicsBody.isAddedToWorld()) {
            physicsBody.mask = physicsBody.mask & ~group;
            physicsBody.reset();
        }
    }

    @Override
    protected void onDetach(Entity entity, OverlappedEntityInfo info) {
        System.out.println("onDetach: " + entity.getId());
        PhysicsBody physicsBody = entity.getComponent(PhysicsBody.class);
        if (physicsBody != null && physicsBody.isAddedToWorld()) {
            physicsBody.mask = physicsBody.mask | group;
            physicsBody.reset();
        }
    }
}
