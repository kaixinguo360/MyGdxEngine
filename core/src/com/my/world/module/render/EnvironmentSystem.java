package com.my.world.module.render;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.my.world.core.Entity;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.render.enhanced.EnhancedEnvironment;
import com.my.world.module.render.enhanced.EnhancedLight;
import lombok.Getter;

public class EnvironmentSystem extends BaseSystem {

    @Getter
    private final EnhancedEnvironment commonEnvironment = new EnhancedEnvironment();
    private final EnhancedEnvironment environment = new EnhancedEnvironment();

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(EnvironmentAttribute.class) || entity.contain(Light.class) || entity.contain(EnhancedLight.class);
    }

    @Override
    public void dispose() {
        commonEnvironment.clear();
        environment.clear();
    }

    // ----- Custom ----- //

    public Environment getEnvironment() {
        environment.clear();
        environment.set(commonEnvironment);
        for (Entity entity : getEntities()) {
            for (EnvironmentAttribute attribute : entity.getComponents(EnvironmentAttribute.class)) {
                if (attribute.isActive()) {
                    environment.set(attribute.getAttribute());
                }
            }
            for (Light light : entity.getComponents(Light.class)) {
                if (light.isActive()) {
                    environment.add(light.getLight());
                }
            }
            for (EnhancedLight light : entity.getComponents(EnhancedLight.class)) {
                if (light.isActive()) {
                    environment.add(light);
                }
            }
        }
        return environment;
    }
}
