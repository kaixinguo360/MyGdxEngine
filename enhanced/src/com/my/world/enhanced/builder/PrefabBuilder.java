package com.my.world.enhanced.builder;

import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Prefab;
import com.my.world.core.Scene;

import java.util.Map;

public abstract class PrefabBuilder<T extends PrefabBuilder<T>> extends BaseBuilder<T> {

    public String prefabName = null;
    public Prefab prefab = null;

    @Override
    public T init(Engine engine, Scene scene) {
        if (prefabName == null) throw new RuntimeException("PrefabBuilder.prefabName can not be null");

        super.init(engine, scene);
        scene.createPrefab(s -> {
            createPrefab(s);
            return prefabName;
        });
        prefab = engine.getAssetsManager().getAsset(prefabName, Prefab.class);

        return (T) this;
    }

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        if (params == null) {
            return scene.instantiatePrefab(prefab);
        } else {
            return scene.instantiatePrefab(prefab, params);
        }
    }

    // ----- Abstract Method ----- //

    public abstract void createPrefab(Scene scene);
}
