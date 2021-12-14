package com.my.utils.world.loader;

import com.my.utils.world.Component;
import com.my.utils.world.LoadContext;
import com.my.utils.world.Loader;

import java.util.HashMap;
import java.util.Map;

public class ComponentLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Component create error: " + e.getMessage(), e);
        }
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
        return (E) new HashMap<String, Object>();
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (Component.class.isAssignableFrom(targetType));
    }
}
