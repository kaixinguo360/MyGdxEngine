package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class World implements Disposable {

    @Getter
    private final SystemManager systemManager = new SystemManager(this);

    @Getter
    private final AssetsManager assetsManager = new AssetsManager(this);

    @Getter
    private final EntityManager entityManager = new EntityManager(this);

    @Getter
    private final Map<String, Object> environments = new HashMap<>();

    public void start() {
        entityManager.updateFilters();
        for (System system : systemManager.getSystems().values()) {
            if (system instanceof System.OnStart) {
                ((System.OnStart) system).start(this);
            }
        }
        entityManager.getBatch().commit();
    }

    public void update(float deltaTime) {
        entityManager.updateFilters();
        for (System system : systemManager.getSystems().values()) {
            if (system instanceof System.OnUpdate) {
                ((System.OnUpdate) system).update(deltaTime);
            }
        }
        entityManager.getBatch().commit();
    }

    public void keyDown(int keycode) {
        for (System system : systemManager.getSystems().values()) {
            if (system instanceof System.OnKeyDown) {
                ((System.OnKeyDown) system).keyDown(keycode);
            }
        }
    }

    @Override
    public void dispose() {
        systemManager.dispose();
        entityManager.dispose();
    }
}
