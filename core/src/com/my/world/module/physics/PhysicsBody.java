package com.my.world.module.physics;

import com.my.world.core.Component;
import com.my.world.core.Entity;
import com.my.world.core.Scene;

public interface PhysicsBody extends Component, Component.Activatable {

    // ----- Physics System ----- //

    void registerToPhysicsSystem(Scene scene, Entity entity, PhysicsSystem physicsSystem);

    void unregisterFromPhysicsSystem(Scene scene, Entity entity, PhysicsSystem physicsSystem);

    Entity getEntity();

    default void collision(PhysicsBody targetBody) {
    }

    // ----- Dynamics World ----- //

    void enterWorld();

    void leaveWorld();

    void reenterWorld();
}
