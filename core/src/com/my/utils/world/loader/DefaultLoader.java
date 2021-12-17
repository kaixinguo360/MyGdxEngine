package com.my.utils.world.loader;

import com.my.utils.world.LoadContext;
import com.my.utils.world.Loadable;
import com.my.utils.world.Loader;

public class DefaultLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        if (Loadable.class.isAssignableFrom(type)) {
            try {
                Loadable<E> loadable = (Loadable<E>) type.newInstance();
                if (loadable.handleable(config.getClass())) {
                    loadable.load(config, context);
                    return (T) loadable;
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Loadable create error: " + e.getMessage(), e);
            }
        }
        throw new RuntimeException("Can not load this config: " + config);
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
        if (obj instanceof Loadable) {
            Loadable<E> loadable = (Loadable<E>) obj;
            if (loadable.handleable(configType)) {
                return loadable.getConfig(configType, context);
            }
        }
        throw new RuntimeException("Can not get config from this obj: " + obj);
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return Loadable.class.isAssignableFrom(targetType);
    }
}
