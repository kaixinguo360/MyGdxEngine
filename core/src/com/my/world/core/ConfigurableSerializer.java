package com.my.world.core;

import com.my.world.core.util.Lambdas;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ConfigurableSerializer implements Serializer, Serializer.Setter {

    protected <E, T> Configurable newInstance(E configObj, Class<T> type, Context context) {
        Configurable configurable;
        try {
            configurable = (Configurable) type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Load Loadable Resource(" + type + ") error: " + e.getMessage(), e);
        }
        return configurable;
    }

    @Override
    public <E, T> T load(E configObj, Class<T> type, Context context) {
        if (Lambdas.isSerializedLambdaConfig(configObj)) return (T) Lambdas.load(configObj);
        Map<String, Object> config = (Map<String, Object>) configObj;

        Configurable configurable;

        configurable = newInstance(configObj, type, context);

        boolean isLazy = false;
        if (configurable instanceof Configurable.OnLoad) {
            try {
                ((Configurable.OnLoad) configurable).load(config, context);
            } catch (EntityManager.EntityManagerException e) {
                if (!context.contains(Configurable.CONTEXT_LAZY_LIST)) {
                    throw e;
                } else {
                    List<Configurable.LazyContext> lazyList = context.get(Configurable.CONTEXT_LAZY_LIST, List.class);

                    Configurable.LazyContext lazyContext = Configurable.LazyContext.obtain(configurable, context);
                    lazyList.add(lazyContext);
                    lazyContext.add(c -> {
                        ((Configurable.OnLoad) configurable).load(config, context);
                    });

                    isLazy = true;
                }
            }
        } else {
            isLazy = Configurable.load(configurable, config, context);
        }

        if (!isLazy && configurable instanceof Configurable.OnInit) {
            ((Configurable.OnInit) configurable).init();
        }

        return (T) configurable;
    }

    @Override
    public <E, T> E dump(T loadable, Class<E> configType, Context context) {
        if (Lambdas.isSerializableLambda(loadable)) return (E) Lambdas.dump(loadable);
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
