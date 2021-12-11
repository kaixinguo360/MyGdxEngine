package com.my.utils.world;

public interface Loader {
    <E, T> T load(E config, Class<T> type);
    <E, T> boolean handleable(Class<E> configType, Class<T> targetType);
}
