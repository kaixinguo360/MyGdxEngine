package com.my.world.module.render;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.my.world.core.Entity;
import com.my.world.module.common.BaseSystem;
import lombok.Getter;

public class EnvironmentSystem extends BaseSystem {

    @Getter
    private final Environment commonEnvironment = new Environment();
    private final Environment environment = new Environment();

    @Override
    public boolean canHandle(Entity entity) {
        return entity.contain(EnvironmentAttribute.class) || entity.contain(Light.class);
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
        }
        return environment;
    }
}
