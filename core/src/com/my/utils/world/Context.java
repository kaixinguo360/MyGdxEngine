package com.my.utils.world;

public interface Context {

    <T> T setEnvironment(String id, T value);

    <T> T getEnvironment(String id, Class<T> type);

    void clearEnvironments();
}
