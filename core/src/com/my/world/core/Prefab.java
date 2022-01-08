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
public class Prefab implements Loadable {

    @Getter
    @Setter
    @Config(name = "entities", type = Config.Type.Primitive)
    private List<Map<String, Object>> entityConfigs;

    public Entity newInstance(Scene scene) {
        return newInstance(scene, this.entityConfigs);
    }

    public Entity newInstance(Scene scene, Map<String, Object> configs) {
        return newInstance(scene, this.entityConfigs, configs);
    }

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

    public static Prefab create(Entity entity, Context context) {
        LoaderManager loaderManager = context.getEnvironment(LoaderManager.CONTEXT_FIELD_NAME, LoaderManager.class);

        List<Map<String, Object>> entityConfigs = new LinkedList<>();
        getConfig(entity, loaderManager, context, entityConfigs);

        return new Prefab(entityConfigs);
    }

    private static void getConfig(Entity entity, LoaderManager loaderManager, Context context, List<Map<String, Object>> entities) {
        entities.add(loaderManager.dump(entity, Map.class, context));
        for (Entity childEntity : entity.getChildren()) {
            getConfig(childEntity, loaderManager, context, entities);
        }
    }
}
