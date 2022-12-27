package com.my.world.enhanced.entity;

import com.my.world.module.render.Render;

import java.util.function.Supplier;

public class RenderEntity extends EnhancedEntity {

    public final Render render;

    public RenderEntity(Builder p) {
        super(p);
        this.render = addComponent(p.renderProvider.get());
    }

    public RenderEntity(Render render) {
        this.render = addComponent(render);
    }

    public static class Builder extends Param {

        public Supplier<Render> renderProvider;

        @Override
        public RenderEntity build() {
            return new RenderEntity(this);
        }
    }
}
