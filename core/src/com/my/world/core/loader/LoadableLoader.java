package com.my.world.core.loader;

import com.my.world.core.Context;
import com.my.world.core.Loadable;
import com.my.world.core.Loader;
import com.my.world.core.Loaders;

import java.util.Map;

public class LoadableLoader implements Loader {

    @Override
    public <E, T> T load(E configObj, Class<T> type, Context context) {
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
    public <E, T> E dump(T loadable, Class<E> configType, Context context) {
        if (loadable instanceof Loadable.OnDump) {
            return (E) ((Loadable.OnDump) loadable).dump(context);
        } else {
            return (E) Loaders.dump((Loadable) loadable, context);
        }
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return Map.class.isAssignableFrom(configType) && Loadable.class.isAssignableFrom(targetType);
    }

}
