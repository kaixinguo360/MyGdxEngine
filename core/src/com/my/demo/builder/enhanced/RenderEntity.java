package com.my.demo.builder.enhanced;

import com.my.world.module.render.Render;

public class RenderEntity extends EnhancedEntity {

    public static class Param extends EnhancedEntity.Param {
        public Render render;
    }

    public final Render render;

    public RenderEntity(Param p) {
        super(p);
        this.render = addComponent(p.render);
    }

    public RenderEntity(Render render) {
        this.render = addComponent(render);
    }
}
