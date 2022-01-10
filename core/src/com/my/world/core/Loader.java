package com.my.world.core;

public interface Loader {

    <E, T> T load(E config, Class<T> type, Context context);
    <E, T> E dump(T obj, Class<E> configType, Context context);
    <E, T> boolean handleable(Class<E> configType, Class<T> targetType);

    interface Setter {
        void set(Object sourceObj, Object targetObj);
        boolean setterHandleable(Class<?> sourceType, Class<?> targetType);
    }
}
