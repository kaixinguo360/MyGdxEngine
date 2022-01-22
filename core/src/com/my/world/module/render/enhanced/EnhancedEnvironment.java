package com.my.world.module.render.enhanced;

import com.badlogic.gdx.graphics.g3d.Environment;

public class EnhancedEnvironment extends Environment {

    public void add(EnhancedLight light) {
        if (light instanceof EnhancedPointLight) {
            add((EnhancedPointLight) light);
        }
    }

    public void add(EnhancedPointLight light) {
        EnhancedPointLightsAttribute attribute = (EnhancedPointLightsAttribute) get(EnhancedPointLightsAttribute.Type);
        if (attribute == null) set(attribute = new EnhancedPointLightsAttribute());
        attribute.lights.add(light);
    }
}
