package com.my.demo.entity.house.door;

import com.my.demo.entity.house.BoxEntity;
import com.my.world.enhanced.EnhancedContext;

public class EdgeEntity extends BoxEntity {

    public EdgeEntity(EnhancedContext context) {
        super(getReturn(() -> {
            EnhancedContext c = context.subContext();
            c.copy("BoxLength", "EdgeDepth", 1f);
            c.copy("BoxHeight", "EdgeWidth", 0.05f);
            c.copy("BoxWidth", "EdgeLength", 2f);
            c.copy("BoxDensity", "EdgeDensity", 0f);
            c.copy("BoxMaterial", "EdgeMaterial", null);
            c.copy("BoxAttributes", "EdgeAttributes", null);
            return c;
        }));
        setName("Edge");
    }
}
