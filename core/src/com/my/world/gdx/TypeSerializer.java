package com.my.world.gdx;

import com.my.world.core.Context;
import com.my.world.core.Engine;
import com.my.world.core.Serializer;

public class TypeSerializer implements Serializer {

    @Override
    public <E, T> T load(E config, Class<T> type, Context context) {
        String name = (String) config;
        Class<?> result;
        try {
            result = context.get(Engine.CONTEXT_FIELD_NAME, Engine.class).getJarManager().loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No such class error: " + e.getMessage(), e);
        }
        return (T) result;
    }

    @Override
    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        Class<?> type = (Class<?>) obj;
        return (E) type.getName();
    }

    @Override
    public <E, T> boolean canSerialize(Class<E> configType, Class<T> targetType) {
        return (configType == Object.class || configType == String.class) && targetType == Class.class;
    }
}
