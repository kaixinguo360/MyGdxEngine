package com.my.utils.world.com;

import com.my.utils.world.Loader;

import java.util.HashMap;
import java.util.Map;

public class RigidBodyLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type) {
        // TODO
        return (T) new RigidBody();
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType) {
        // TODO
        return (E) new HashMap<String, Object>();
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == RigidBody.class);
    }
}
