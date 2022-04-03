package com.my.world.core;

import com.my.world.core.util.Disposable;
import com.my.world.core.util.OverlayMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.function.Function;

@NoArgsConstructor
@AllArgsConstructor
public class Prefab implements Configurable {

    @Getter
    @Setter
    @Config(name = "entities", type = Config.Type.Primitive)
    private List<Map<String, Object>> entityConfigs;

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
        Engine engine = scene.getEngine();
        SerializerManager serializerManager = scene.getEngine().getSerializerManager();
        EntityManager entityManager = scene.getEntityManager();
        Map<String, String> tmpIdMap = new HashMap<>();

        Function<String, Entity> originalEntityProvider = context.getEnvironment(EntityManager.CONTEXT_ENTITY_PROVIDER, Function.class);
        Function<Entity, Entity> originalEntityAdder = context.getEnvironment(EntityManager.CONTEXT_ENTITY_ADDER, Function.class);
        String prefix = (context.containsEnvironment(EntityManager.CONTEXT_ENTITY_PREFIX)
                ? context.getEnvironment(EntityManager.CONTEXT_ENTITY_PREFIX, String.class)
                : UUID.randomUUID().toString()) + '.';

        Function<String, Entity> oriEntityProvider = (originalEntityProvider != null)
                ? originalEntityProvider
                : entityManager::findEntityById;
        Function<Entity, Entity> oriEntityAdder = (originalEntityAdder != null)
                ? originalEntityAdder
                : entityManager::addEntity;

        Function<String, Entity> entityProvider = (id) -> {
            if (!tmpIdMap.isEmpty() && tmpIdMap.containsKey(id)) {
                id = tmpIdMap.get(id);
            }
            try {
                return oriEntityProvider.apply(prefix + id);
            } catch (EntityManager.EntityManagerException e) {
                return oriEntityProvider.apply(id);
            }
        };
        context.setEnvironment(EntityManager.CONTEXT_ENTITY_PROVIDER, entityProvider);
        Function<Entity, Entity> entityAdder = (entity) -> {
            if (entity.getId() != null) {
                entity.setId(prefix + entity.getId());
            }
            return oriEntityAdder.apply(entity);
        };
        context.setEnvironment(EntityManager.CONTEXT_ENTITY_ADDER, entityAdder);

        Entity firstEntity = null;
        for (Map<String, Object> map : entityConfigs) {
            Entity entity;

            Class<? extends Entity> entityType = null;
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
                context.setEnvironment(EntityManager.CONTEXT_ENTITY_PREFIX, globalId);
                entity = scene.instantiatePrefab(prefabName, prefabConfig, context);
                context.setEnvironment(EntityManager.CONTEXT_ENTITY_PREFIX, prefix);
            } else {
                Object entityConfig = map.get("config");
                entity = serializerManager.load(entityConfig, entityType, context);
                if (entity.getId() != null) {
                    tmpIdMap.put(entity.getId(), globalId);
                    entity.setId(globalId);
                }
                oriEntityAdder.apply(entity);
            }
            if (firstEntity == null) firstEntity = entity;
        }

        context.setEnvironment(EntityManager.CONTEXT_ENTITY_PROVIDER, originalEntityProvider);
        context.setEnvironment(EntityManager.CONTEXT_ENTITY_ADDER, originalEntityAdder);

        return firstEntity;
    }
}
