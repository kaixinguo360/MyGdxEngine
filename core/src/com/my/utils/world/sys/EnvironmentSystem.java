package com.my.utils.world.sys;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.my.utils.world.*;
import com.my.utils.world.com.Light;
import lombok.Getter;

import java.util.Map;

public class EnvironmentSystem extends BaseSystem {

    @Getter
    private final Environment commonEnvironment = new Environment();
    private final Environment environment = new Environment();

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(Light.class);
    }

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        // TODO: Better Common Environment Loader
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        Environment commonEnvironment = assetsManager.getAsset("commonEnvironment", Environment.class);
        this.commonEnvironment.set(commonEnvironment);
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