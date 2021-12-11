package com.my.utils.world.loader;

import com.my.utils.world.Component;
import com.my.utils.world.Loader;
import com.my.utils.world.LoaderManager;

import java.util.HashMap;
import java.util.Map;

public class ComponentLoader implements Loader {

    private LoaderManager loaderManager;

    public ComponentLoader(LoaderManager loaderManager) {
        this.loaderManager = loaderManager;
    }

    @Override
    public <E, T> T load(E config, Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Component create error: " + e.getMessage(), e);
        }
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType) {
        return (E) new HashMap<String, Object>();
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (Component.class.isAssignableFrom(targetType));
    }
}
