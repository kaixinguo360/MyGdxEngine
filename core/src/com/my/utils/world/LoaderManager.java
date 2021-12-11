package com.my.utils.world;

import com.my.utils.world.com.*;
import com.my.utils.world.loader.DefaultLoader;
import com.my.utils.world.loader.EntityLoader;
import com.my.utils.world.loader.SystemLoader;
import com.my.utils.world.loader.WorldLoader;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoaderManager {

    @Getter
    private final List<Loader> loaders = new ArrayList<>();

    @Getter
    private final Map<String, Object> environment = new HashMap<>();

    private final Map<String, Loader> cache = new HashMap<>();

    public LoaderManager() {
        loaders.add(new WorldLoader(this));
        loaders.add(new SystemLoader(this));
        loaders.add(new EntityLoader(this));
        loaders.add(new CollisionLoader(this));
        loaders.add(new MotionLoader(this));
        loaders.add(new PositionLoader());
        loaders.add(new RenderLoader(this));
        loaders.add(new RigidBodyLoader(this));
        loaders.add(new SerializationLoader());
        loaders.add(new DefaultLoader());
    }

    public <T, E> T load(E config, Class<T> type) {
        if (config == null) {
            throw new RuntimeException("No such loader: null -> " + type);
        }
        String hash = config.getClass() + " -> " + type;
        if (cache.containsKey(hash)) {
            return cache.get(hash).load(config, type);
        } else {
            for (Loader loader : loaders) {
                if (loader.handleable(config.getClass(), type)) {
                    cache.put(hash, loader);
                    return loader.load(config, type);
                }
            }
        }
        throw new RuntimeException("No such loader: " + config.getClass() + " -> " + type);
    }

    public <T, E> E getConfig(T obj, Class<E> configType) {
        String hash = configType + " -> " + obj.getClass();
        if (cache.containsKey(hash)) {
            return cache.get(hash).getConfig(obj, configType);
        } else {
            for (Loader loader : loaders) {
                if (loader.handleable(configType, obj.getClass())) {
                    cache.put(hash, loader);
                    return loader.getConfig(obj, configType);
                }
            }
        }
        throw new RuntimeException("No such loader to get config: " + configType + " -> " + obj.getClass());
    }

    public <T extends Loader> T getLoader(Class<T> type) {
        for (Loader loader : loaders) {
            if (type.isInstance(loader)) {
                return (T) loader;
            }
        }
        return null;
    }
}
