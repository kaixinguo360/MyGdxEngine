package com.my.utils.world.loader;

import com.my.utils.world.System;
import com.my.utils.world.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

    @Getter
    @Setter
    Consumer<World> beforeLoad;

    public WorldLoader(LoaderManager loaderManager) {
        this.loaderManager = loaderManager;
    }

    @Override
    public <E, T> T load(E config, Class<T> type) {
        World world = new World();
        beforeLoad.accept(world);
        loaderManager.getEnvironment().put("world", world);

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
                    boolean assetProvided = asset.containsKey("provided") && (Boolean) asset.get("provided");
                    if (!assetProvided) {
                        if (!assetsManager.hasAsset(assetId, assetType)) {
                            assetsManager.addAsset(assetId, assetType, loaderManager.load(assetConfig, assetType));
                        } else {
                            java.lang.System.out.println("Asset already loaded: " + assetId + " (" + assetType.getName() + ")");
                        }
                    }
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
    public <E, T> E getConfig(T obj, Class<E> configType) {
        World world = (World) obj;
        Map<String, Object> map = new HashMap<>();

        Map<Class<?>, System> systems = world.getSystemManager().getSystems();
        if (systems.size() > 0) {
            List<Map<String, Object>> systemList = new ArrayList<>();
            for (System system : systems.values()) {
                Map<String, Object> systemMap = new HashMap<>();
                systemMap.put("type", system.getClass().getName());
                systemMap.put("config", loaderManager.getConfig(system, Map.class));
                systemList.add(systemMap);
            }
            map.put("systems", systemList);
        }

        Map<Class<?>, Map<String, Object>> assets = world.getAssetsManager().getAllAssets();
        if (assets.size() > 0) {
            List<Map<String, Object>> assetList = new ArrayList<>();
            for (Map.Entry<Class<?>, Map<String, Object>> entry : assets.entrySet()) {
                Class<?> assetType = entry.getKey();
                for (Map.Entry<String, Object> assetEntry : entry.getValue().entrySet()) {
                    Map<String, Object> assetMap = new HashMap<>();
                    assetMap.put("id", assetEntry.getKey());
                    assetMap.put("type", assetType.getName());
                    try {
                        assetMap.put("config", loaderManager.getConfig(assetEntry.getValue(), Map.class));
                    } catch (RuntimeException e) {
                        assetMap.put("provided", true);
                    }
                    assetList.add(assetMap);
                }
            }
            map.put("assets", assetList);
        }

        Map<String, Entity> entities = world.getEntityManager().getEntities();
        if (entities.size() > 0) {
            List<Map<String, Object>> entityList = new ArrayList<>();
            for (Map.Entry<String, Entity> entry : entities.entrySet()) {
                Entity entity = entry.getValue();
                Map<String, Object> entityMap = new HashMap<>();
                entityMap.put("type", entity.getClass().getName());
                entityMap.put("config", loaderManager.getConfig(entity, Map.class));
                entityList.add(entityMap);
            }
            map.put("entities", entityList);
        }

        return (E) map;
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == World.class);
    }
}
