package com.my.utils.world.com;

import com.my.utils.world.*;
import com.my.utils.world.sys.ConstraintSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Constraint implements Component, LoadableResource {

    public String bodyA;
    public String bodyB;
    public ConstraintSystem.ConstraintType type;
    public Map<String, Object> config;
    public ConstraintSystem.ConstraintController controller;

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        ConstraintSystem.ConstraintController constraintController = null;
        if (config.containsKey("controllerType") && config.containsKey("controllerConfig")) {
            try {
                Class<?> controllerType = Class.forName((String) config.get("controllerType"));
                Map<String, Object> controllerConfig = (Map<String, Object>) config.get("controllerConfig");
                constraintController = (ConstraintSystem.ConstraintController) context.getLoaderManager().load(controllerConfig, controllerType, context);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Constraint controller create error: " + e.getMessage(), e);
            }
        }
        this.bodyA = (String) config.get("bodyA");
        this.bodyB = (String) config.get("bodyB");
        this.type = assetsManager.getAsset((String) config.get("type"), ConstraintSystem.ConstraintType.class);
        this.config = (Map<String, Object>) config.get("config");
        this.controller = constraintController;
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        return new HashMap<String, Object>() {{
            put("bodyA", bodyA);
            put("bodyB", bodyB);
            put("type", assetsManager.getId(ConstraintSystem.ConstraintType.class, type));
            put("config", config);
            if (controller != null) {
                put("controllerType", controller.getClass().getName());
                put("controllerConfig", context.getLoaderManager().getConfig(controller, HashMap.class, context));
            }
        }};
    }
}
