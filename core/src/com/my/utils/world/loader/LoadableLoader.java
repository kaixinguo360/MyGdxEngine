package com.my.utils.world.loader;

import com.my.utils.world.LoadContext;
import com.my.utils.world.Loadable;
import com.my.utils.world.Loader;
import com.my.utils.world.Loaders;

import java.util.Map;

public class LoadableLoader implements Loader {

    @Override
    public <E, T> T load(E configObj, Class<T> type, LoadContext context) {
        Map<String, Object> config = (Map<String, Object>) configObj;

        Loadable loadable;

        try {
            loadable = (Loadable) type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Load Loadable Resource(" + type + ") error: " + e.getMessage(), e);
        }

        if (loadable instanceof Loadable.OnLoad) {
            ((Loadable.OnLoad) loadable).load(config, context);
        } else {
            Loaders.load(loadable, config, context);
        }

        if (loadable instanceof Loadable.OnInit) {
            ((Loadable.OnInit) loadable).init();
        }

        return (T) loadable;

    }

    @Override
    public <E, T> E getConfig(T loadable, Class<E> configType, LoadContext context) {
        if (loadable instanceof Loadable.OnGetConfig) {
            return (E) ((Loadable.OnGetConfig) loadable).getConfig(context);
        } else {
            return (E) Loaders.getConfig((Loadable) loadable, context);
        }
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return Map.class.isAssignableFrom(configType) && Loadable.class.isAssignableFrom(targetType);
    }

}
