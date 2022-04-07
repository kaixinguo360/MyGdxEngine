package com.my.world.core;

import com.my.world.core.util.Disposable;
import com.my.world.core.util.OverlayMap;

import java.util.*;
import java.util.function.Function;

public class EntityUtil {

    public static final String CONTEXT_ENTITY_PROVIDER = "ENTITY_PROVIDER";
    public static final String CONTEXT_ENTITY_ADDER = "ENTITY_ADDER";
    public static final String CONTEXT_ENTITY_PREFIX = "ENTITY_PREFIX";
    public static final String CONTEXT_ENTITY_PREFAB_FLAG = "ENTITY_PREFAB_FLAG";

    public static Entity newInstance(Scene scene, List<Map<String, Object>> entityConfigs, Map<String, Object> overlayConfigs, Context context) {
        if (overlayConfigs == null) {
            return newInstance(scene, entityConfigs, context);
        } else {
            List<Map<String, Object>> tmpEntityConfigs = new ArrayList<>();
            for (Map<String, Object> entityConfig : entityConfigs) {
                Map<String, Object> config = (Map<String, Object>) entityConfig.get("config");
                String name = (String) config.get("name");
                if (name == null) {
                    String prefabName = (String) entityConfig.get("prefabName");
                    name = (String) config.get(prefabName + ".config.name");
                }
                tmpEntityConfigs.add(OverlayMap.obtain(entityConfig, overlayConfigs, name));
            }
            Entity entity = newInstance(scene, tmpEntityConfigs, context);
            Disposable.disposeAll(tmpEntityConfigs);
            return entity;
        }
    }

    public static Entity newInstance(Scene scene, List<Map<String, Object>> entityConfigs, Context context) {
        Boolean originalPrefabFlag = context.set(CONTEXT_ENTITY_PREFAB_FLAG, true);
        Entity entity = loadEntities(scene, entityConfigs, context);
        context.set(CONTEXT_ENTITY_PREFAB_FLAG, originalPrefabFlag);
        return entity;
    }

    public static Entity loadEntities(Scene scene, List<Map<String, Object>> entityConfigs, Context context) {
        Engine engine = scene.getEngine();
        SerializerManager serializerManager = scene.getEngine().getSerializerManager();
        EntityManager entityManager = scene.getEntityManager();
        Map<String, String> tmpIdMap = new HashMap<>();

        Function<String, Entity> entityProvider = context.get(CONTEXT_ENTITY_PROVIDER, Function.class,
                (Function<String, Entity>) entityManager::findEntityById);
        Function<Entity, Entity> entityAdder = context.get(CONTEXT_ENTITY_ADDER, Function.class,
                (Function<Entity, Entity>) entityManager::addEntity);
        boolean prefabFlag = context.get(CONTEXT_ENTITY_PREFAB_FLAG, Boolean.class, false);
        String prefix = context.get(CONTEXT_ENTITY_PREFIX, String.class, null);

        Function<String, Entity> originalEntityProvider = context.set(CONTEXT_ENTITY_PROVIDER, (id) -> {
            if (!tmpIdMap.isEmpty() && tmpIdMap.containsKey(id)) {
                id = tmpIdMap.get(id);
            }
            if (prefix != null) {
                try {
                    return entityProvider.apply(prefix + id);
                } catch (EntityManager.EntityManagerException e) {
                    return entityProvider.apply(id);
                }
            } else {
                return entityProvider.apply(id);
            }
        });
        Function<Entity, Entity> originalEntityAdder = context.set(CONTEXT_ENTITY_ADDER, (entity) -> {
            if (prefix != null && entity.getId() != null) {
                entity.setId(prefix + entity.getId());
            }
            return entityAdder.apply(entity);
        });

        Entity firstEntity = null;
        for (Map<String, Object> map : entityConfigs) {
            Entity entity;

            Class<? extends Entity> entityType;
            try {
                entityType = (Class<? extends Entity>) engine.getJarManager().loadClass((String) map.get("type"));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("No such class error: " + e.getMessage(), e);
            }

            String globalId = (String) map.get("globalId");
            if (globalId == null) {
                globalId = UUID.randomUUID().toString();
            }

            if (Prefab.class.isAssignableFrom(entityType)) {
                String prefabName = (String) map.get("prefabName");
                Map<String, Object> prefabConfig = (Map<String, Object>) map.get("config");
                String originalPrefix = context.set(CONTEXT_ENTITY_PREFIX, globalId);
                entity = scene.instantiatePrefab(prefabName, prefabConfig, context);
                context.set(CONTEXT_ENTITY_PREFIX, originalPrefix);
            } else {
                Object entityConfig = map.get("config");
                entity = serializerManager.load(entityConfig, entityType, context);
                if (prefabFlag && entity.getId() != null) {
                    tmpIdMap.put(entity.getId(), globalId);
                    entity.setId(globalId);
                }
                entityAdder.apply(entity);
            }
            if (firstEntity == null) firstEntity = entity;
        }

        context.set(CONTEXT_ENTITY_PROVIDER, originalEntityProvider);
        context.set(CONTEXT_ENTITY_ADDER, originalEntityAdder);

        return firstEntity;
    }
}
