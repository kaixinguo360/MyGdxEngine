package com.my.demo.entity.house;

import com.my.world.enhanced.EnhancedContext;

public class FloorEntity extends BoxEntity {

    public FloorEntity(EnhancedContext context) {
        super(getReturn(() -> {
            EnhancedContext c = context.subContext();
            c.copy("BoxHeight", "FloorThickness", 1f);
            c.copy("BoxWidth", "FloorWidth", 1f);
            c.copy("BoxLength", "FloorLength", 1f);
            c.copy("BoxDensity", "FloorDensity", 0f);
            c.copy("BoxMaterial", "FloorMaterial", null);
            c.copy("BoxAttributes", "FloorAttributes", null);
            return c;
        }));
        setName("Floor");
    }
}
