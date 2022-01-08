package com.my.world.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Prefab implements Loadable {

    private static final EntityManager entityManager = new EntityManager();

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

    public static Entity newInstance(Scene scene, List<Map<String, Object>> entityConfigs, Map<String, Object> overlayConfigs) {
        if (overlayConfigs == null) {
            return newInstance(scene, entityConfigs);
        } else {
            List<Map<String, Object>> tmpEntityConfigs = new ArrayList<>();
            for (Map<String, Object> entityConfig : entityConfigs) {
                String name = (String) entityConfig.get("name");
                tmpEntityConfigs.add(new OverlayMap<>(entityConfig, overlayConfigs, name));
            }
            return newInstance(scene, tmpEntityConfigs);
        }
    }

    public static Entity newInstance(Scene scene, List<Map<String, Object>> entityConfigs) {
        LoaderManager loaderManager = scene.getEngine().getLoaderManager();

        Context context = scene.newContext();
        context.setEnvironment(EntityManager.CONTEXT_FIELD_NAME, entityManager);

        for (Map<String, Object> map : entityConfigs) {
            Entity entity = loaderManager.load(map, Entity.class, context);
            entityManager.addEntity(entity);
        }

        Entity entity = scene.getEntityManager().addAll(entityManager, true);
        entityManager.clear();

        return entity;
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
