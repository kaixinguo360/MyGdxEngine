package com.my.utils.world;

import com.my.utils.world.loader.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoaderManager {

    @Getter
    protected final List<Loader> loaders = new ArrayList<>();
    private final Map<String, Loader> loaderCache = new HashMap<>();

    public LoaderManager() {
        loaders.add(new WorldLoader());
        loaders.add(new Matrix4Loader());
        loaders.add(new Vector3Loader());
        loaders.add(new QuaternionLoader());
        loaders.add(new LoadableLoader());
    }

    public <E, T> T load(E config, Class<T> type) {
        return load(config, type, new LoadContextImpl());
    }

    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        if (config == null) {
            throw new RuntimeException("No such loader: null -> " + type);
        }
        String hash = config.getClass() + " -> " + type;
        if (loaderCache.containsKey(hash)) {
            return loaderCache.get(hash).load(config, type, context);
        } else {
            for (Loader loader : loaders) {
                if (loader.handleable(config.getClass(), type)) {
                    loaderCache.put(hash, loader);
                    return loader.load(config, type, context);
                }
            }
        }
        throw new RuntimeException("No such loader: " + config.getClass() + " -> " + type);
    }

    public <E, T> E getConfig(T obj, Class<E> configType) {
        return getConfig(obj, configType, new LoadContextImpl());
    }

    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
        String hash = configType + " -> " + obj.getClass();
        if (loaderCache.containsKey(hash)) {
            return loaderCache.get(hash).getConfig(obj, configType, context);
        } else {
            for (Loader loader : loaders) {
                if (loader.handleable(configType, obj.getClass())) {
                    loaderCache.put(hash, loader);
                    return loader.getConfig(obj, configType, context);
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

    public LoadContext newContext() {
        return new LoadContextImpl();
    }

    private class LoadContextImpl implements LoadContext {

        private final Map<String, Object> environment = new HashMap<>();

        private LoadContextImpl() {}

        @Override
        public LoaderManager getLoaderManager() {
            return LoaderManager.this;
        }

        @Override
        public <T> T setEnvironment(String id, T value) {
            if (id == null) throw new RuntimeException("Environment variable id can not be null");
            if (value == null) throw new RuntimeException("Environment variable value can not be null");
            environment.put(id, value);
            return value;
        }

        @Override
        public <T> T getEnvironment(String id, Class<T> type) {
            if (!environment.containsKey(id) || environment.get(id) == null) throw new RuntimeException("No such environment variable: " + id);
            if (!type.isInstance(environment.get(id))) throw new RuntimeException("Unmatched Environment variable type: " + environment.get(id).getClass() + " != " + type);
            return type.cast(environment.get(id));
        }
    }
}
