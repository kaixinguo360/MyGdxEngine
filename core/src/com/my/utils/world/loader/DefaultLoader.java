package com.my.utils.world.loader;

import com.my.utils.world.Loader;

public class DefaultLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type) {
        return (T) config;
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return configType == targetType;
    }
}
