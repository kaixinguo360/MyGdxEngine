package com.my.utils.world;

public interface Loader {
    <E, T> T load(E config, Class<T> type, Context context);
    <E, T> E getConfig(T obj, Class<E> configType, Context context);
    <E, T> boolean handleable(Class<E> configType, Class<T> targetType);
}
