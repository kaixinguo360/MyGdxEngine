package com.my.world.enhanced.builder;

import com.my.world.core.AssetsManager;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.core.SerializerManager;

public abstract class BaseBuilder<T extends BaseBuilder<T>> implements EntityBuilder {

    protected Engine engine;
    protected AssetsManager assetsManager;
    protected SerializerManager serializerManager;

    @Override
    public T init(Engine engine, Scene scene) {
        this.engine = engine;
        this.assetsManager = engine.getAssetsManager();
        this.serializerManager = engine.getSerializerManager();

        initDependencies();
        createAssets(engine, scene);

        String name = this.getClass().getName();
        assetsManager.addAsset(name, EntityBuilder.class, this);

        return (T) this;
    }

    // ----- Abstract Method ----- //

    protected void initDependencies() {}

    protected void createAssets(Engine engine, Scene scene) {}

    // ----- Util Method ----- //

    public <E extends EntityBuilder> E getDependency(Class<E> dependency) {
        try {
            return (E) assetsManager.getAsset(dependency.getName(), EntityBuilder.class);
        } catch (RuntimeException e) {
            throw new DependenciesException("No such builder: " + dependency);
        }
    }

}
