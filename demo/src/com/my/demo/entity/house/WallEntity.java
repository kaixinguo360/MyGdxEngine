package com.my.demo.entity.house;

import com.my.world.enhanced.EnhancedContext;

public class WallEntity extends BoxEntity {

    public final static short WALL_FLAG = 1 << 6;

    public WallEntity(EnhancedContext context) {
        super(getReturn(() -> {
            EnhancedContext c = context.subContext();
            c.copy("BoxLength", "WallThickness", 1f);
            c.copy("BoxHeight", "WallHeight", 1f);
            c.copy("BoxWidth", "WallLength", 1f);
            c.copy("BoxDensity", "WallDensity", 0f);
            c.copy("BoxMaterial", "WallMaterial", null);
            c.copy("BoxAttributes", "WallAttributes", null);
            return c;
        }));
        setName("Wall");
        rigidBody.group = WALL_FLAG;
    }
}
