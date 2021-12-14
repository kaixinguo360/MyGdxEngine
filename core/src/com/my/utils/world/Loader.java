package com.my.utils.world;

public interface Loader {
    <E, T> T load(E config, Class<T> type, LoadContext context);
    <E, T> E getConfig(T obj, Class<E> configType, LoadContext context);
    <E, T> boolean handleable(Class<E> configType, Class<T> targetType);
}
