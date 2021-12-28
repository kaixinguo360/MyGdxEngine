package com.my.utils.world;

import com.my.utils.world.loader.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoaderManager {

    public static final String CONTEXT_FIELD_NAME = "LOADER_MANAGER";

    @Getter
    protected final List<Loader> loaders = new ArrayList<>();
    private final Map<String, Loader> loaderCache = new HashMap<>();

    @Getter
    protected final Context context = new Context();

    public LoaderManager() {
        loaders.add(new SceneLoader());
        loaders.add(new Matrix4Loader());
        loaders.add(new Vector3Loader());
        loaders.add(new QuaternionLoader());
        loaders.add(new LoadableLoader());
        this.context.setEnvironment(CONTEXT_FIELD_NAME, this);
    }

    public <E, T> T load(E config, Class<T> type) {
        return load(config, type, newContext());
    }

    public <E, T> T load(E config, Class<T> type, Context context) {
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

    public <E, T> E dump(T obj, Class<E> configType) {
        return dump(obj, configType, newContext());
    }

    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        String hash = configType + " -> " + obj.getClass();
        if (loaderCache.containsKey(hash)) {
            return loaderCache.get(hash).dump(obj, configType, context);
        } else {
            for (Loader loader : loaders) {
                if (loader.handleable(configType, obj.getClass())) {
                    loaderCache.put(hash, loader);
                    return loader.dump(obj, configType, context);
                }
            }
        }
        throw new RuntimeException("No such loader to get config: " + configType + " -> " + obj.getClass());
    }

    public <T extends Loader> T findLoader(Class<T> type) {
        for (Loader loader : loaders) {
            if (type.isInstance(loader)) {
                return (T) loader;
            }
        }
        return null;
    }

    public Context newContext() {
        return this.context.newContext();
    }
}
