package com.my.utils.world;

public interface LoadContext {

    LoaderManager getLoaderManager();

    <T> T setEnvironment(String id, T value);

    <T> T getEnvironment(String id, Class<T> type);
}
