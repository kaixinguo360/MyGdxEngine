package com.my.world.core;

import com.my.world.core.util.Disposable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Scene implements Disposable {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String id;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Status status = Status.Created;

    @Getter
    private final String name;

    @Getter
    private final Engine engine;

    @Getter
    private final Context context;

    @Getter
    private final SystemManager systemManager = new SystemManager(this);

    @Getter
    private final EntityManager entityManager = new EntityManager();

    Scene(Engine engine, String name) {
        this.name = name;
        this.engine = engine;
        this.context = engine.newContext();
        this.context.setEnvironment(EntityManager.CONTEXT_ENTITY_PROVIDER, (Function<String, Entity>) entityManager::findEntityById);
    }

    public void start() {
        for (System.OnStart system : systemManager.getSystems(System.OnStart.class)) {
            system.start(this);
        }
    }

    public void update(float deltaTime) {
        entityManager.updateFilters();
        for (System.OnUpdate system : systemManager.getSystems(System.OnUpdate.class)) {
            system.update(deltaTime);
        }
        entityManager.getBatch().commit();
    }

    public Context newContext() {
        return this.context.newContext();
    }

    // ----- Prefab ----- //
    public Prefab dumpToPrefab() {
        LoaderManager loaderManager = getEngine().getLoaderManager();

        List<Map<String, Object>> entityConfigs = new LinkedList<>();
        for (Entity entity : entityManager.getEntities().values()) {
            String name = entity.getName();
            if ("tmp".equalsIgnoreCase(name)) continue;
            entityConfigs.add(loaderManager.dump(entity, Map.class, context));
        }
        entityManager.clearEntity();

        return new Prefab(entityConfigs);
    }
    public Entity instantiatePrefab(Prefab prefab) {
        return Prefab.newInstance(this, prefab.getEntityConfigs());
    }
    public Entity instantiatePrefab(Prefab prefab, Map<String, Object> configs) {
        return Prefab.newInstance(this, prefab.getEntityConfigs(), configs);
    }
    public Entity instantiatePrefab(String prefabName) {
        Prefab prefab = engine.getAssetsManager().getAsset(prefabName, Prefab.class);
        return instantiatePrefab(prefab);
    }
    public Entity instantiatePrefab(String prefabName, Map<String, Object> configs) {
        Prefab prefab = engine.getAssetsManager().getAsset(prefabName, Prefab.class);
        return Prefab.newInstance(this, prefab.getEntityConfigs(), configs);
    }

    // ----- Load & Dump ----- //
    public <E, T> T load(E config, Class<T> type) {
        return engine.getLoaderManager().load(config, type, newContext());
    }
    public <E, T> E dump(T obj, Class<E> configType) {
        return engine.getLoaderManager().dump(obj, configType, newContext());
    }

    @Override
    public void dispose() {
        entityManager.dispose();
        systemManager.dispose();
    }

    public enum Status {
        Created, Running, Deleted, Disposed
    }
}
