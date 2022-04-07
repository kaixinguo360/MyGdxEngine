package com.my.world.core;

import com.my.world.core.util.Disposable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
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

    @Getter
    private final EventManager eventManager = new EventManager();

    Scene(Engine engine, String name) {
        this.name = name;
        this.engine = engine;
        this.engine.getEventManager().getChildren().add(eventManager);
        this.context = engine.subContext();
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

    public Context subContext() {
        return this.context.subContext();
    }

    public <T> T subContext(Function<Context, T> fun) {
        return this.context.subContext(fun);
    }

    // ----- Prefab ----- //
    public Prefab dumpToPrefab() {
        SerializerManager serializerManager = getEngine().getSerializerManager();

        List<Map<String, Object>> entityConfigs = new LinkedList<>();
        for (Entity entity : entityManager.getEntities().values()) {
            String name = entity.getName();
            if ("tmp".equalsIgnoreCase(name)) continue;
            Map<String, Object> config = serializerManager.dump(entity, Map.class, context);
            config.put("globalId", null);
            entityConfigs.add(new HashMap<String, Object>() {{
                put("type", "com.my.world.core.Entity");
                put("config", config);
            }});
        }
        entityManager.clearEntity();

        return new Prefab(entityConfigs);
    }
    public Entity instantiatePrefab(Prefab prefab) {
        return subContext(c -> EntityUtil.newInstance(this, prefab.getEntityConfigs(), c));
    }
    public Entity instantiatePrefab(Prefab prefab, Map<String, Object> configs) {
        return subContext(c -> EntityUtil.newInstance(this, prefab.getEntityConfigs(), configs, c));
    }
    public Entity instantiatePrefab(Prefab prefab, Map<String, Object> configs, Context context) {
        return EntityUtil.newInstance(this, prefab.getEntityConfigs(), configs, context);
    }
    public Entity instantiatePrefab(String prefabName) {
        Prefab prefab = engine.getAssetsManager().getAsset(prefabName, Prefab.class);
        return subContext(c -> EntityUtil.newInstance(this, prefab.getEntityConfigs(), c));
    }
    public Entity instantiatePrefab(String prefabName, Map<String, Object> configs) {
        Prefab prefab = engine.getAssetsManager().getAsset(prefabName, Prefab.class);
        return subContext(c -> EntityUtil.newInstance(this, prefab.getEntityConfigs(), configs, c));
    }
    public Entity instantiatePrefab(String prefabName, Map<String, Object> configs, Context context) {
        Prefab prefab = engine.getAssetsManager().getAsset(prefabName, Prefab.class);
        return EntityUtil.newInstance(this, prefab.getEntityConfigs(), configs, context);
    }

    // ----- Load & Dump ----- //
    public <E, T> T load(E config, Class<T> type) {
        return subContext(c -> engine.getSerializerManager().load(config, type, c));
    }
    public <E, T> E dump(T obj, Class<E> configType) {
        return subContext(c -> engine.getSerializerManager().dump(obj, configType, c));
    }

    // ----- Util Method ----- //
    public Entity addEntity(Entity entity) {
        return entityManager.addEntity(entity);
    }
    public Entity tmpEntity() {
        Entity entity = new Entity();
        entity.setName("tmp");
        return entityManager.addEntity(entity);
    }
    public <T> T getAsset(String id, Class<T> type) {
        return engine.getAssetsManager().getAsset(id, type);
    }
    public void createPrefab(Function<Scene, String> function) {
        String name = function.apply(this);
        Prefab prefab = dumpToPrefab();
        engine.getAssetsManager().addAsset(name, Prefab.class, prefab);
    }

    @Override
    public void dispose() {
        entityManager.dispose();
        systemManager.dispose();

        this.engine.getEventManager().getChildren().remove(eventManager);
        eventManager.dispose();
    }

    public enum Status {
        Created, Running, Deleted, Disposed
    }
}
