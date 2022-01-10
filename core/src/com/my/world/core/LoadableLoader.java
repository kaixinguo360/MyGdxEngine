package com.my.world.core;

import java.util.Collection;
import java.util.Map;

public class LoadableLoader implements Loader, Loader.Setter {

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
            Loadable.load(loadable, config, context);
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
            return (E) Loadable.dump((Loadable) loadable, context);
        }
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return Map.class.isAssignableFrom(configType) && Loadable.class.isAssignableFrom(targetType);
    }

    @Override
    public void set(Object sourceObj, Object targetObj) {
        Collection source = (Collection) sourceObj;
        Collection target = (Collection) targetObj;
        target.clear();
        target.addAll(source);
    }

    @Override
    public boolean setterHandleable(Class<?> sourceType, Class<?> targetType) {
        return Collection.class.isAssignableFrom(sourceType) && Collection.class.isAssignableFrom(targetType);
    }
}
