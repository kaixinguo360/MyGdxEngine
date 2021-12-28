package com.my.world.module.render;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.my.world.core.Entity;
import com.my.world.module.common.BaseSystem;
import lombok.Getter;

public class EnvironmentSystem extends BaseSystem {

    @Getter
    private final Environment commonEnvironment = new Environment();
    private final Environment environment = new Environment();

    public EnvironmentSystem() {
        // TODO: Better Common Environment Loader
        Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.2f, -0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.2f, 0.8f, -1f));
        this.commonEnvironment.set(environment);
    }

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(Light.class);
    }

    // ----- Custom ----- //

    public Environment getEnvironment() {
        environment.clear();
        environment.set(commonEnvironment);
        for (Entity entity : getEntities()) {
            Light light = entity.getComponent(Light.class);
            if (light != null) {
                environment.add(light.light);
            }
        }
        return environment;
    }
}
