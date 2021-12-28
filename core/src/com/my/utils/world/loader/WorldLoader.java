package com.my.utils.world.loader;

import com.my.utils.world.System;
import com.my.utils.world.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    @Override
    public <E, T> T load(E config, Class<T> type, Context context) {
        AssetsManager assetsManager = context.getEnvironment(AssetsManager.CONTEXT_FIELD_NAME, AssetsManager.class);
        World world = new World(assetsManager);

        context.setEnvironment(EntityManager.CONTEXT_FIELD_NAME, world.getEntityManager());

        try {
            Map<String, Object> map = (Map<String, Object>) config;

            SystemManager systemManager = world.getSystemManager();
            List<Map<String, Object>> systems = (List<Map<String, Object>>) map.get("systems");
            if (systems != null) {
                for (Map<String, Object> system : systems) {
                    Class<? extends System> systemType = (Class<? extends System>) Class.forName((String) system.get("type"));
                    Object systemConfig = system.get("config");
                    systemManager.addSystem(context.getEnvironment(LoaderManager.CONTEXT_FIELD_NAME, LoaderManager.class).load(systemConfig, systemType, context));
                }
            }
            world.start();

            EntityManager entityManager = world.getEntityManager();
            List<Map<String, Object>> entities = (List<Map<String, Object>>) map.get("entities");
            if (entities != null) {
                for (Map<String, Object> entity : entities) {
                    Class<? extends Entity> entityType = (Class<? extends Entity>) Class.forName((String) entity.get("type"));
                    Object entityConfig = entity.get("config");
                    entityManager.addEntity(context.getEnvironment(LoaderManager.CONTEXT_FIELD_NAME, LoaderManager.class).load(entityConfig, entityType, context));
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No such class error: " + e.getMessage(), e);
        }

        return (T) world;
    }

    @Override
    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        World world = (World) obj;
        Map<String, Object> map = new LinkedHashMap<>();

        context.setEnvironment(AssetsManager.CONTEXT_FIELD_NAME, world.getAssetsManager());
        context.setEnvironment(EntityManager.CONTEXT_FIELD_NAME, world.getEntityManager());

        Map<String, Entity> entities = world.getEntityManager().getEntities();
        if (entities.size() > 0) {
            List<Map<String, Object>> entityList = new ArrayList<>();
            for (Map.Entry<String, Entity> entry : entities.entrySet()) {
                Entity entity = entry.getValue();
                Map<String, Object> entityMap = new LinkedHashMap<>();
                entityMap.put("type", entity.getClass().getName());
                entityMap.put("config", context.getEnvironment(LoaderManager.CONTEXT_FIELD_NAME, LoaderManager.class).dump(entity, Map.class, context));
                entityList.add(entityMap);
            }
            map.put("entities", entityList);
        }

        Map<Class<?>, System> systems = world.getSystemManager().getSystems();
        if (systems.size() > 0) {
            List<Map<String, Object>> systemList = new ArrayList<>();
            for (System system : systems.values()) {
                Map<String, Object> systemMap = new LinkedHashMap<>();
                systemMap.put("type", system.getClass().getName());
                systemMap.put("config", context.getEnvironment(LoaderManager.CONTEXT_FIELD_NAME, LoaderManager.class).dump(system, Map.class, context));
                systemList.add(systemMap);
            }
            map.put("systems", systemList);
        }

        return (E) map;
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == World.class);
    }
}
