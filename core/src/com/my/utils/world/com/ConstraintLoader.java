package com.my.utils.world.com;

import com.my.utils.world.AssetsManager;
import com.my.utils.world.Loader;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.World;
import com.my.utils.world.sys.ConstraintSystem;

import java.util.HashMap;
import java.util.Map;

public class ConstraintLoader implements Loader {

    private LoaderManager loaderManager;
    private AssetsManager assetsManager;

    public ConstraintLoader(LoaderManager loaderManager) {
        this.loaderManager = loaderManager;
    }

    private AssetsManager getAssetsManager() {
        World world = (World) loaderManager.getEnvironment().get("world");
        if (world == null) throw new RuntimeException("Required params not set: world");
        return world.getAssetsManager();
    }

    @Override
    public <E, T> T load(E config, Class<T> type) {
        if (assetsManager == null) assetsManager = getAssetsManager();
        Map<String, Object> map = (Map<String, Object>) config;
        return (T) new Constraint(
                (String) map.get("bodyA"),
                (String) map.get("bodyB"),
                assetsManager.getAsset((String) map.get("type"), ConstraintSystem.ConstraintType.class),
                (Map<String, Object>) map.get("config"),
                null // TODO: Controller
        );
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType) {
        if (assetsManager == null) assetsManager = getAssetsManager();
        Constraint constraint = (Constraint) obj;
        return (E) new HashMap<String, Object>() {{
            put("bodyA", constraint.bodyA);
            put("bodyB", constraint.bodyB);
            put("type", assetsManager.getId(ConstraintSystem.ConstraintType.class, constraint.type));
            put("config", constraint.config);
            // TODO: Controller
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == Constraint.class);
    }
}
