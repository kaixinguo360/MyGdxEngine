package com.my.utils.world.com;

import com.my.utils.world.AssetsManager;
import com.my.utils.world.LoadContext;
import com.my.utils.world.Loader;
import com.my.utils.world.World;
import com.my.utils.world.sys.ConstraintSystem;

import java.util.HashMap;
import java.util.Map;

public class ConstraintLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        Map<String, Object> map = (Map<String, Object>) config;
        ConstraintSystem.ConstraintController constraintController = null;
        if (map.containsKey("controllerType") && map.containsKey("controllerConfig")) {
            try {
                Class<?> controllerType = Class.forName((String) map.get("controllerType"));
                Map<String, Object> controllerConfig = (Map<String, Object>) map.get("controllerConfig");
                constraintController = (ConstraintSystem.ConstraintController) context.getLoaderManager().load(controllerConfig, controllerType, context);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Constraint controller create error: " + e.getMessage(), e);
            }
        }
        return (T) new Constraint(
                (String) map.get("bodyA"),
                (String) map.get("bodyB"),
                assetsManager.getAsset((String) map.get("type"), ConstraintSystem.ConstraintType.class),
                (Map<String, Object>) map.get("config"),
                constraintController
        );
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        Constraint constraint = (Constraint) obj;
        return (E) new HashMap<String, Object>() {{
            put("bodyA", constraint.bodyA);
            put("bodyB", constraint.bodyB);
            put("type", assetsManager.getId(ConstraintSystem.ConstraintType.class, constraint.type));
            put("config", constraint.config);
            if (constraint.controller != null) {
                put("controllerType", constraint.controller.getClass().getName());
                put("controllerConfig", context.getLoaderManager().getConfig(constraint.controller, HashMap.class, context));
            }
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == Constraint.class);
    }
}
