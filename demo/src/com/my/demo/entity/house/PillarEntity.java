package com.my.demo.entity.house;

import com.my.world.enhanced.EnhancedContext;

public class PillarEntity extends BoxEntity {

    public PillarEntity(EnhancedContext context) {
        super(getReturn(() -> {
            EnhancedContext c = context.subContext();
            c.copy("BoxHeight", "PillarHeight", 1f);
            c.copy("BoxWidth", "PillarThick", 1f);
            c.copy("BoxLength", "PillarThick", 1f);
            c.copy("BoxDensity", "PillarDensity", 0f);
            c.copy("BoxMaterial", "PillarMaterial", null);
            c.copy("BoxAttributes", "PillarAttributes", null);
            return c;
        }));
        setName("Pillar");
    }
}
