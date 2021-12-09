package com.my.utils.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

import java.util.Map;

public class World implements Disposable {

    @Getter
    private final SystemManager systemManager = new SystemManager();

    @Getter
    private final EntityManager entityManager = new EntityManager();

    @Getter
    private final AssetsManager assetsManager = new AssetsManager();

    // ----- Update ----- //
    public void update() {
        for (Map.Entry<Class<?>, System> entry : systemManager.getSystems().entrySet()) {
            System system = entry.getValue();
            Array<Entity> sortEntities = system.getEntities();
            sortEntities.clear();
            for (Entity entity : entityManager.getEntities().values()) {
                if (system.isHandleable(entity)) {
                    sortEntities.add(entity);
                }
            }
        }
    }

    @Override
    public void dispose() {
        systemManager.dispose();
        entityManager.dispose();
    }
}
