package com.my.utils.world.com;

import com.my.utils.world.AssetsManager;
import com.my.utils.world.LoadContext;
import com.my.utils.world.Loader;
import com.my.utils.world.World;
import com.my.utils.world.sys.PhysicsSystem;

import java.util.HashMap;
import java.util.Map;

public class RigidBodyLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        Map<String, Object> map = (Map<String, Object>) config;
        String bodyConfigId = (String) map.get("bodyConfigId");
        PhysicsSystem.RigidBodyConfig rigidBodyConfig = assetsManager.getAsset(bodyConfigId, PhysicsSystem.RigidBodyConfig.class);
        return (T) rigidBodyConfig.newInstance();
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
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
