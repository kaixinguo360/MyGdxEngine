package com.my.utils.world.loader;

import com.my.utils.world.Loader;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.System;

import java.util.HashMap;
import java.util.Map;

public class SystemLoader implements Loader {

    private LoaderManager loaderManager;

    public SystemLoader(LoaderManager loaderManager) {
        this.loaderManager = loaderManager;
    }

    @Override
    public <E, T> T load(E config, Class<T> type) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("System create error: " + e.getMessage(), e);
        }
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType) {
        return (E) new HashMap<String, String>();
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (System.class.isAssignableFrom(targetType));
    }
}
