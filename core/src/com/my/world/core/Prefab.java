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

    private static final List<Map<String, Object>> tmpEntityConfigs = new ArrayList<>();
    public static Entity newInstance(Scene scene, List<Map<String, Object>> entityConfigs, Map<String, Object> overlayConfigs) {
        if (overlayConfigs == null) {
            return newInstance(scene, entityConfigs);
        } else {
            tmpEntityConfigs.clear();
            for (Map<String, Object> entityConfig : entityConfigs) {
                String name = (String) entityConfig.get("name");
                tmpEntityConfigs.add(OverlayMap.obtain(entityConfig, overlayConfigs, name));
            }
            Entity entity = newInstance(scene, tmpEntityConfigs);
            Disposable.disposeAll(tmpEntityConfigs);
            return entity;
        }
    }

    private static final Map<String, String> tmpIdMap = new HashMap<>();
    public static Entity newInstance(Scene scene, List<Map<String, Object>> entityConfigs) {
        SerializerManager serializerManager = scene.getEngine().getSerializerManager();
        EntityManager entityManager = scene.getEntityManager();
        String prefix = UUID.randomUUID() + "_";
        tmpIdMap.clear();

        Context context = scene.newContext();
        context.setEnvironment(EntityManager.CONTEXT_ENTITY_PROVIDER, (Function<String, Entity>) id -> {
            if (!tmpIdMap.isEmpty() && tmpIdMap.containsKey(id)) {
                return entityManager.findEntityById(tmpIdMap.get(id));
            } else {
                try {
                    return entityManager.findEntityById(prefix + id);
                } catch (EntityManager.EntityManagerException e) {
                    return entityManager.findEntityById(id);
                }
            }
        });

        Entity firstEntity = null;
        for (Map<String, Object> map : entityConfigs) {
            Entity entity = serializerManager.load(map, Entity.class, context);
            if (entity.getId() != null) {
                String globalId = (String) map.get("globalId");
                if (globalId != null) {
                    tmpIdMap.put(entity.getId(), globalId);
                    entity.setId(globalId);
                } else {
                    entity.setId(prefix + entity.getId());
                }
            }
            if (firstEntity == null) firstEntity = entity;
            entityManager.addEntity(entity);
        }

        return firstEntity;
    }
}
