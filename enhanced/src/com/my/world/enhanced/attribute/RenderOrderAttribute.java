package com.my.world.enhanced.attribute;

import com.badlogic.gdx.graphics.g3d.Attribute;

public class RenderOrderAttribute extends Attribute {

    public static final String RenderOrderAlias = "renderOrder";
    public static final long RenderOrder = register(RenderOrderAlias);

    public int order;

    public RenderOrderAttribute(int order) {
        super(RenderOrder);
        this.order = order;
    }

    @Override
    public Attribute copy() {
        return new RenderOrderAttribute(this.order);
    }

    @Override
    public int compareTo(Attribute o) {
        return 0;
    }
}
