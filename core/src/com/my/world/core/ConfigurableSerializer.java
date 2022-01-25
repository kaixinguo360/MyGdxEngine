package com.my.world.core;

import java.util.Collection;
import java.util.Map;

public class ConfigurableSerializer implements Serializer, Serializer.Setter {

    @Override
    public <E, T> T load(E configObj, Class<T> type, Context context) {
        Map<String, Object> config = (Map<String, Object>) configObj;

        Configurable configurable;

        try {
            configurable = (Configurable) type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Load Loadable Resource(" + type + ") error: " + e.getMessage(), e);
        }

        if (configurable instanceof Configurable.OnLoad) {
            ((Configurable.OnLoad) configurable).load(config, context);
        } else {
            Configurable.load(configurable, config, context);
        }

        if (configurable instanceof Configurable.OnInit) {
            ((Configurable.OnInit) configurable).init();
        }

        return (T) configurable;

    }

    @Override
    public <E, T> E dump(T loadable, Class<E> configType, Context context) {
        if (loadable instanceof Configurable.OnDump) {
            return (E) ((Configurable.OnDump) loadable).dump(context);
        } else {
            return (E) Configurable.dump((Configurable) loadable, context);
        }
    }

    @Override
    public <E, T> boolean canSerialize(Class<E> configType, Class<T> targetType) {
        return Map.class.isAssignableFrom(configType) && Configurable.class.isAssignableFrom(targetType);
    }

    @Override
    public void set(Object sourceObj, Object targetObj) {
        Collection source = (Collection) sourceObj;
        Collection target = (Collection) targetObj;
        target.clear();
        target.addAll(source);
    }

    @Override
    public boolean canSet(Class<?> sourceType, Class<?> targetType) {
        return Collection.class.isAssignableFrom(sourceType) && Collection.class.isAssignableFrom(targetType);
    }
}
