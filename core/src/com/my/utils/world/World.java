package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

public class World implements Disposable {

    @Getter
    private final AssetsManager assetsManager = new AssetsManager(this);

    @Getter
    private final SystemManager systemManager = new SystemManager(this);

    @Getter
    private final EntityManager entityManager = new EntityManager(this);

    // ----- Update ----- //
    public void update() {
        entityManager.updateFilters();
    }

    @Override
    public void dispose() {
        systemManager.dispose();
        entityManager.dispose();
    }
}
