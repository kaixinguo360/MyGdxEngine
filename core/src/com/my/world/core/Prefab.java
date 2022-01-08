package com.my.world.core;

import com.my.world.core.util.Disposable;
import com.my.world.core.util.OverlayMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@NoArgsConstructor
@AllArgsConstructor
public class Prefab implements Loadable {

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

    public static Entity newInstance(Scene scene, List<Map<String, Object>> entityConfigs) {
        LoaderManager loaderManager = scene.getEngine().getLoaderManager();
        EntityManager entityManager = scene.getEntityManager();
        String prefix = UUID.randomUUID() + "_";

        Context context = scene.newContext();
        context.setEnvironment(EntityManager.CONTEXT_ENTITY_PROVIDER, (Function<String, Entity>) id -> entityManager.findEntityById(prefix + id));

        Entity firstEntity = null;
        for (Map<String, Object> map : entityConfigs) {
            Entity entity = loaderManager.load(map, Entity.class, context);
            if (entity.getId() != null) entity.setId(prefix + entity.getId());
            if (firstEntity == null) firstEntity = entity;
            entityManager.addEntity(entity);
        }

        return firstEntity;
    }
}
