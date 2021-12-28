package com.my.utils.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public Entity newInstance(LoaderManager loaderManager, Scene scene) {
        return newInstance(this, loaderManager, scene);
    }

    public static Entity newInstance(Prefab prefab, LoaderManager loaderManager, Scene scene) {
        Context context = loaderManager.newContext();

        context.setEnvironment(AssetsManager.CONTEXT_FIELD_NAME, scene.getAssetsManager());
        context.setEnvironment(EntityManager.CONTEXT_FIELD_NAME, entityManager);

        for (Map<String, Object> map : prefab.entityConfigs) {
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
