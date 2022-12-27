package com.my.world.enhanced.entity;

import com.my.world.module.physics.RigidBody;
import com.my.world.module.render.Render;

import java.util.function.Supplier;

public class RigidBodyEntity extends RenderEntity {

    public final RigidBody rigidBody;

    public RigidBodyEntity(Builder p) {
        super(p);
        this.rigidBody = addComponent(p.rigidBodyProvider.get());
    }

    public RigidBodyEntity(Render render, RigidBody rigidBody) {
        super(render);
        this.rigidBody = addComponent(rigidBody);
    }

    public static class Builder extends RenderEntity.Builder {

        public Supplier<RigidBody> rigidBodyProvider;

        @Override
        public RigidBodyEntity build() {
            return new RigidBodyEntity(this);
        }
    }
}
