package com.my.utils.world;

import com.my.utils.world.loader.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoaderManager {

    @Getter
    private final List<Loader> loaders = new ArrayList<>();

    private final Map<String, Loader> cache = new HashMap<>();

    public LoaderManager() {
        loaders.add(new WorldLoader(this));
        loaders.add(new SystemLoader(this));
        loaders.add(new EntityLoader(this));
        loaders.add(new ComponentLoader(this));
        loaders.add(new DefaultLoader());
    }

    public <T, E> T load(E config, Class<T> type) {
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
}
