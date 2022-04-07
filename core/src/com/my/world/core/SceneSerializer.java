package com.my.world.core;

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
public class SceneSerializer implements Serializer {

    @Override
    public <E, T> T load(E config, Class<T> type, Context context) {
        Engine engine = context.get(Engine.CONTEXT_FIELD_NAME, Engine.class);
        Scene scene;

        try {
            Map<String, Object> map = (Map<String, Object>) config;

            String name = (String) map.get("name");
            scene = new Scene(engine, name);

            SystemManager systemManager = scene.getSystemManager();
            List<Map<String, Object>> systems = (List<Map<String, Object>>) map.get("systems");
            if (systems != null) {
                for (Map<String, Object> system : systems) {
                    Class<? extends System> systemType = (Class<? extends System>) engine.getJarManager().loadClass((String) system.get("type"));
                    Object systemConfig = system.get("config");
                    systemManager.addSystem(context.get(SerializerManager.CONTEXT_FIELD_NAME, SerializerManager.class).load(systemConfig, systemType, context));
                }
            }

            List<Map<String, Object>> entities = (List<Map<String, Object>>) map.get("entities");
            if (entities != null) {
                EntityUtil.loadEntities(scene, entities, context);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No such class error: " + e.getMessage(), e);
        }

        return (T) scene;
    }

    @Override
    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        Scene scene = (Scene) obj;
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("name", scene.getName());

        Map<String, Entity> entities = scene.getEntityManager().getEntities();
        if (entities.size() > 0) {
            List<Map<String, Object>> entityList = new ArrayList<>();
            for (Map.Entry<String, Entity> entry : entities.entrySet()) {
                Entity entity = entry.getValue();
                Map<String, Object> entityMap = new LinkedHashMap<>();
                entityMap.put("type", entity.getClass().getName());
                entityMap.put("config", context.get(SerializerManager.CONTEXT_FIELD_NAME, SerializerManager.class).dump(entity, Map.class, context));
                entityList.add(entityMap);
            }
            map.put("entities", entityList);
        }

        Map<Class<?>, System> systems = scene.getSystemManager().getSystems();
        if (systems.size() > 0) {
            List<Map<String, Object>> systemList = new ArrayList<>();
            for (System system : systems.values()) {
                Map<String, Object> systemMap = new LinkedHashMap<>();
                systemMap.put("type", system.getClass().getName());
                systemMap.put("config", context.get(SerializerManager.CONTEXT_FIELD_NAME, SerializerManager.class).dump(system, Map.class, context));
                systemList.add(systemMap);
            }
            map.put("systems", systemList);
        }

        return (E) map;
    }

    @Override
    public <E, T> boolean canSerialize(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == Scene.class);
    }

}
