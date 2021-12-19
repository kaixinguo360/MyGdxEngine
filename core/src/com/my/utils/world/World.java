package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class World implements Disposable {

    @Getter
    private final AssetsManager assetsManager = new AssetsManager();

    @Getter
    private final SystemManager systemManager = new SystemManager(this);

    @Getter
    private final EntityManager entityManager = new EntityManager();

    @Getter
    private final Map<String, Object> environments = new HashMap<>();

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

    @Override
    public void dispose() {
        systemManager.dispose();
        entityManager.dispose();
    }
}
