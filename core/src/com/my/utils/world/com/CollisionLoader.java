package com.my.utils.world.com;

import com.my.utils.world.AssetsManager;
import com.my.utils.world.Loader;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.World;
import com.my.utils.world.sys.PhysicsSystem;

import java.util.HashMap;
import java.util.Map;

public class CollisionLoader implements Loader {

    private LoaderManager loaderManager;
    private AssetsManager assetsManager;

    public CollisionLoader(LoaderManager loaderManager) {
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
        return (T) new Collision(
                (int) map.get("callbackFilter"),
                (int) map.get("callbackFlag"),
                assetsManager.getAsset((String) map.get("handlerName"), PhysicsSystem.CollisionHandler.class)
        );
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType) {
        if (assetsManager == null) assetsManager = getAssetsManager();
        Collision collision = (Collision) obj;
        return (E) new HashMap<String, Object>() {{
            put("callbackFilter", collision.callbackFilter);
            put("callbackFlag", collision.callbackFlag);
            put("handlerName", assetsManager.getId(PhysicsSystem.CollisionHandler.class, collision.handler));
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == Collision.class);
    }
}
