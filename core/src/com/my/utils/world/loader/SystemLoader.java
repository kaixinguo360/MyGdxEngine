package com.my.utils.world.loader;

import com.my.utils.world.LoadContext;
import com.my.utils.world.Loader;
import com.my.utils.world.System;

import java.util.HashMap;
import java.util.Map;

public class SystemLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("System create error: " + e.getMessage(), e);
        }
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
        return (E) new HashMap<String, String>();
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (System.class.isAssignableFrom(targetType));
    }
}
