package com.my.utils.world.loader;

import com.my.utils.world.Loader;

public class DefaultLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type) {
        return (T) config;
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType) {
        return (E) obj;
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return configType == targetType;
    }
}
