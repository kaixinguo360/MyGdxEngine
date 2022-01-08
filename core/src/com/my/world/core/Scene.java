package com.my.world.core;

import com.my.world.core.util.Disposable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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
        this.context.setEnvironment(EntityManager.CONTEXT_FIELD_NAME, entityManager);
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

    @Override
    public void dispose() {
        entityManager.dispose();
        systemManager.dispose();
    }

    public enum Status {
        Created, Running, Deleted, Disposed
    }
}
