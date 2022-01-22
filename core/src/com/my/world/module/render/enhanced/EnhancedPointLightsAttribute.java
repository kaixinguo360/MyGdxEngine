package com.my.world.module.render.enhanced;

import com.badlogic.gdx.graphics.g3d.Attribute;

import java.util.ArrayList;
import java.util.List;

public class EnhancedPointLightsAttribute extends Attribute {

    public final static String Alias = "enhancedPointLights";
    public final static long Type = register(Alias);

    public final List<EnhancedPointLight> lights;

    public EnhancedPointLightsAttribute() {
        super(Type);
        lights = new ArrayList<>(1);
    }

    @Override
    public Attribute copy() {
        EnhancedPointLightsAttribute attribute = new EnhancedPointLightsAttribute();
        attribute.lights.addAll(this.lights);
        return attribute;
    }

    @Override
    public int compareTo(Attribute o) {
        if (type != o.type) return type < o.type ? -1 : 1;
        return 0;
    }
}
