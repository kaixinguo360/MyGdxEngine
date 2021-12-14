package com.my.utils.world;

public interface Loadable<T> {
    void load(T config, LoadContext context);
    T getConfig(Class<T> configType, LoadContext context);
    default <E> boolean handleable(Class<E> configType) {
        return true;
    }
}
