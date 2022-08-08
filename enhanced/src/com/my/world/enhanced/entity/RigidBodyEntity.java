package com.my.world.enhanced.entity;

import com.my.world.module.physics.RigidBody;
import com.my.world.module.render.Render;

public class RigidBodyEntity extends RenderEntity {

    public static class Param extends RenderEntity.Param {
        public RigidBody rigidBody;
    }

    public final RigidBody rigidBody;

    public RigidBodyEntity(Param p) {
        super(p);
        this.rigidBody = addComponent(p.rigidBody);
    }

    public RigidBodyEntity(Render render, RigidBody rigidBody) {
        super(render);
        this.rigidBody = addComponent(rigidBody);
    }
}
