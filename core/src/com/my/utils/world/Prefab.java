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

    public Entity newInstance(LoaderManager loaderManager, World world) {
        return newInstance(this, loaderManager, world);
    }

    public static Entity newInstance(Prefab prefab, LoaderManager loaderManager, World world) {
        LoadContext context = loaderManager.newContext();

        context.setEnvironment(Loaders.CONTEXT_ASSETS_MANAGER, world.getAssetsManager());
        context.setEnvironment(Loaders.CONTEXT_SYSTEM_MANAGER, world.getSystemManager());
        context.setEnvironment(Loaders.CONTEXT_ENTITY_MANAGER, entityManager);

        for (Map<String, Object> map : prefab.entityConfigs) {
            Entity entity = loaderManager.load(map, Entity.class, context);
            entityManager.addEntity(entity);
        }

        Entity entity = world.getEntityManager().addAllEntities(entityManager, true);
        entityManager.clear();

        return entity;
    }

    public static Prefab create(Entity entity, LoaderManager loaderManager, World world) {
        return create(entity, loaderManager, world.getAssetsManager(), world.getSystemManager(), world.getEntityManager());
    }

    public static Prefab create(Entity entity, LoaderManager loaderManager, AssetsManager assetsManager, SystemManager systemManager, EntityManager entityManager) {
        LoadContext context = loaderManager.newContext();

        context.setEnvironment(Loaders.CONTEXT_ASSETS_MANAGER, assetsManager);
        context.setEnvironment(Loaders.CONTEXT_SYSTEM_MANAGER, systemManager);
        context.setEnvironment(Loaders.CONTEXT_ENTITY_MANAGER, entityManager);

        List<Map<String, Object>> entityConfigs = new LinkedList<>();
        getConfig(entity, loaderManager, context, entityConfigs);

        return new Prefab(entityConfigs);
    }

    private static void getConfig(Entity entity, LoaderManager loaderManager, LoadContext context, List<Map<String, Object>> entities) {
        entities.add(loaderManager.getConfig(entity, Map.class, context));
        for (Entity childEntity : entity.getChildren()) {
            getConfig(childEntity, loaderManager, context, entities);
        }
    }
}
