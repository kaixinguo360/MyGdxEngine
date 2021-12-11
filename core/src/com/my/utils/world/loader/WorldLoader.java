package com.my.utils.world.loader;

import com.my.utils.world.System;
import com.my.utils.world.*;

import java.util.List;
import java.util.Map;

/**
 * Config Example:
 * <pre>
 *     assets:
 *       - id: Asset_1
 *         type: com.my.asset.Asset1
 *         config: ...
 *       - id: Asset_2
 *         type: com.my.asset.Asset2
 *         config: ...
 *     systems:
 *       - type: com.my.system.System1
 *         config: ...
 *       - type: com.my.system.System2
 *         config: ...
 *     entities:
 *       - type: com.my.entity.Entity1
 *         config: ...
 *       - type: com.my.entity.Entity2
 *         config: ...
 * </pre>
 */
public class WorldLoader implements Loader {

    private LoaderManager loaderManager;

    public WorldLoader(LoaderManager loaderManager) {
        this.loaderManager = loaderManager;
    }

    @Override
    public <E, T> T load(E config, Class<T> type) {
        World world = new World();

        try {
            Map<String, Object> map = (Map<String, Object>) config;

            SystemManager systemManager = world.getSystemManager();
            List<Map<String, Object>> systems = (List<Map<String, Object>>) map.get("systems");
            if (systems != null) {
                for (Map<String, Object> system : systems) {
                    Class<? extends System> systemType = (Class<? extends System>) Class.forName((String) system.get("type"));
                    Object systemConfig = system.get("config");
                    systemManager.addSystem(loaderManager.load(systemConfig, systemType));
                }
            }

            AssetsManager assetsManager = world.getAssetsManager();
            List<Map<String, Object>> assets = (List<Map<String, Object>>) map.get("assets");
            if (assets != null) {
                for (Map<String, Object> asset : assets) {
                    String assetId = (String) asset.get("id");
                    Class<?> assetType = Class.forName((String) asset.get("type"));
                    Object assetConfig = asset.get("config");
                    assetsManager.addAsset(assetId, assetType, loaderManager.load(assetConfig, assetType));
                }
            }

            EntityManager entityManager = world.getEntityManager();
            List<Map<String, Object>> entities = (List<Map<String, Object>>) map.get("entities");
            if (entities != null) {
                for (Map<String, Object> entity : entities) {
                    Class<? extends Entity> entityType = (Class<? extends Entity>) Class.forName((String) entity.get("type"));
                    Object entityConfig = entity.get("config");
                    entityManager.addEntity(loaderManager.load(entityConfig, entityType));
                }
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No such class error: " + e.getMessage(), e);
        }

        return (T) world;
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == World.class);
    }
}
