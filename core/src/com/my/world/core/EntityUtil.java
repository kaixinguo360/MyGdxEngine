package com.my.world.core;

import com.my.world.core.util.Disposable;
import com.my.world.core.util.OverlayMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EntityUtil {

    public static final String CONTEXT_ENTITY_PROVIDER = "ENTITY_PROVIDER";
    public static final String CONTEXT_ENTITY_ADDER = "ENTITY_ADDER";
    public static final String CONTEXT_PREFIX = "PREFIX";

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
        boolean containPrefix = context.contains(CONTEXT_PREFIX);

        if (!containPrefix) context.set(CONTEXT_PREFIX, randomID() + '/');
        Entity entity = loadEntities(scene, entityConfigs, context);
        if (!containPrefix) context.set(CONTEXT_PREFIX, null);

        return entity;
    }

    public static Entity loadEntities(Scene scene, List<Map<String, Object>> entityConfigs, Context context) {
        Engine engine = scene.getEngine();
        SerializerManager serializerManager = scene.getEngine().getSerializerManager();
        EntityManager entityManager = scene.getEntityManager();

        Function<String, Entity> entityProvider = context.get(CONTEXT_ENTITY_PROVIDER, Function.class,
                (Function<String, Entity>) entityManager::findEntityById);
        Function<Entity, Entity> entityAdder = context.get(CONTEXT_ENTITY_ADDER, Function.class,
                (Function<Entity, Entity>) entityManager::addEntity);
        String prefix = context.get(CONTEXT_PREFIX, String.class, null);

        Map<String, String> globalIdMap = null;
        Function<String, Entity> originalEntityProvider;
        Function<Entity, Entity> originalEntityAdder;
        List<Configurable.LazyContext> lazyList = null;

        if (prefix != null) {
            Map<String, String> idMap = new HashMap<>();
            globalIdMap = idMap;

            originalEntityProvider = context.set(CONTEXT_ENTITY_PROVIDER, (id) -> {
                if (!idMap.isEmpty() && idMap.containsKey(id)) {
                    id = idMap.get(id);
                }
                try {
                    return entityProvider.apply(prefix + id);
                } catch (EntityManager.EntityManagerException e) {
                    return entityProvider.apply(id);
                }
            });

            originalEntityAdder = context.set(CONTEXT_ENTITY_ADDER, (entity) -> {
                if (entity.getId() != null) {
                    entity.setId(prefix + entity.getId());
                }
                return entityAdder.apply(entity);
            });
        } else {
            originalEntityProvider = context.set(CONTEXT_ENTITY_PROVIDER, entityProvider);
            originalEntityAdder = context.set(CONTEXT_ENTITY_ADDER, entityAdder);

            lazyList = new ArrayList<>();
            context.set(Configurable.CONTEXT_LAZY_LIST, lazyList);
        }

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

            if (Prefab.class.isAssignableFrom(entityType)) {
                String prefabName = (String) map.get("prefabName");
                Map<String, Object> prefabConfig = (Map<String, Object>) map.get("config");
                if (globalId == null) {
                    globalId = randomID();
                }
                String originalPrefix = context.set(CONTEXT_PREFIX, globalId + '/');
                entity = scene.instantiatePrefab(prefabName, prefabConfig, context);
                context.set(CONTEXT_PREFIX, originalPrefix);
            } else {
                Object entityConfig = map.get("config");
                entity = serializerManager.load(entityConfig, entityType, context);
                if (prefix != null && entity.getId() != null) {
                    if (globalId == null) {
                        globalId = prefix + entity.getId();
                    }
                    globalIdMap.put(entity.getId(), globalId);
                    entity.setId(globalId);
                }
                entityAdder.apply(entity);
            }
            if (firstEntity == null) firstEntity = entity;
        }

        if (prefix == null) {
            for (Configurable.LazyContext lazyContext : lazyList) {
                lazyContext.load();
                lazyContext.dispose();
            }
            lazyList.clear();
        }

        context.set(CONTEXT_ENTITY_PROVIDER, originalEntityProvider);
        context.set(CONTEXT_ENTITY_ADDER, originalEntityAdder);
        context.set(Configurable.CONTEXT_LAZY_LIST, null);

        return firstEntity;
    }

    public static int nextId = 0;
    public static String randomID() {
        return String.format("%03d", nextId++);
    }
}
