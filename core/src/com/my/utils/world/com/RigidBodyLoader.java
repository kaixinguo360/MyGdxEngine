package com.my.utils.world.com;

import com.my.utils.world.AssetsManager;
import com.my.utils.world.Loader;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.World;
import com.my.utils.world.sys.PhysicsSystem;

import java.util.HashMap;
import java.util.Map;

public class RigidBodyLoader implements Loader {

    private LoaderManager loaderManager;
    private AssetsManager assetsManager;

    public RigidBodyLoader(LoaderManager loaderManager) {
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
        String bodyConfigId = (String) map.get("bodyConfigId");
        PhysicsSystem.RigidBodyConfig rigidBodyConfig = assetsManager.getAsset(bodyConfigId, PhysicsSystem.RigidBodyConfig.class);
        return (T) rigidBodyConfig.newInstance();
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType) {
        if (assetsManager == null) assetsManager = getAssetsManager();
        RigidBody rigidBody = (RigidBody) obj;
        String bodyConfigId = assetsManager.getId(PhysicsSystem.RigidBodyConfig.class, rigidBody.bodyConfig);
        return (E) new HashMap<String, Object>(){{
            put("bodyConfigId", bodyConfigId);
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == RigidBody.class);
    }
}
