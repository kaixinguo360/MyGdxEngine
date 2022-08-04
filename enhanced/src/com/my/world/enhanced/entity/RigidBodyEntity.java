package com.my.world.enhanced.entity;

import com.my.world.module.physics.RigidBody;

public class RigidBodyEntity extends RenderEntity {

    public static class Param extends RenderEntity.Param {
        public RigidBody rigidBody;
    }

    public final RigidBody rigidBody;

    public RigidBodyEntity(Param p) {
        super(p);
        this.rigidBody = addComponent(p.rigidBody);
    }
}
