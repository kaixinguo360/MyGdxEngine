package com.my.utils.world;

public interface Loader {
    <E, T> T load(E config, Class<T> type);
    <E, T> E getConfig(T obj, Class<E> configType);
    <E, T> boolean handleable(Class<E> configType, Class<T> targetType);
}
