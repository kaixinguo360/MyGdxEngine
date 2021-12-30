package com.my.world.core;

import lombok.Getter;

public class Scene implements Disposable {

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
        entityManager.updateFilters();
        for (System.OnStart system : systemManager.getSystems(System.OnStart.class)) {
            system.start(this);
        }
        entityManager.getBatch().commit();
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
}
