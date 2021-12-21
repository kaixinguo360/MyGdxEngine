package com.my.utils.world.loader;

import com.my.utils.world.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

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
            try {
                for (Field field : getFields(loadable)) {
                    field.setAccessible(true);
                    Config annotation = field.getAnnotation(Config.class);
                    String name = annotation.name();
                    if ("".equals(name)) name = field.getName();
                    Object obj = getObject(context, field.getType(), annotation, config.get(name));
                    if (Modifier.isFinal(field.getModifiers())) {
                        if (!(field.get(loadable) != null && List.class.isAssignableFrom(field.getType()) && obj instanceof Collection)) {
                            throw new RuntimeException("Can not set a final field: " + field);
                        }
                        List<Object> fieldList = (List<Object>) field.get(loadable);
                        fieldList.clear();
                        fieldList.addAll((Collection<?>) obj);
                    } else {
                        field.set(loadable, obj);
                    }
                }
            } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException("Load Loadable Resource(" + type + ") error: " + e.getMessage(), e);
            }
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
            Map<String, Object> map = new LinkedHashMap<>();
            try {
                for (Field field : getFields(loadable)) {
                    field.setAccessible(true);
                    Config annotation = field.getAnnotation(Config.class);
                    String name = annotation.name();
                    if ("".equals(name)) name = field.getName();
                    Object obj = getConfig(context, field.getType(), annotation, field.get(loadable));
                    map.put(name, obj);
                }
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException("Get config from Loadable Resource(" + loadable.getClass() + ") error: " + e.getMessage(), e);
            }
            return (E) map;
        }
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return Map.class.isAssignableFrom(configType) && Loadable.class.isAssignableFrom(targetType);
    }

    private Object getObject(LoadContext context, Class<?> elementType, Config annotation, Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {

        World world = context.getEnvironment("world", World.class);
        EntityManager entityManager = world.getEntityManager();
        AssetsManager assetsManager = world.getAssetsManager();

        if (value == null) {
            return null;
        } else if (List.class.isAssignableFrom(elementType)) {
            List<Object> objList = new ArrayList<>();
            for (Object value1 : (List<Object>) value) {
                objList.add(getObject(context, annotation.elementType(), annotation, value1));
            }
            return objList;
        } else if (annotation.type() == Config.Type.Primitive || elementType.isPrimitive() || elementType == String.class) {
            if ((elementType == float.class || elementType == Float.class) && Number.class.isAssignableFrom(value.getClass())) {
                value = ((Number) value).floatValue();
            }
            return value;
        } else if (annotation.type() == Config.Type.Asset) {
            String assetId = (String) value;
            return assetsManager.getAsset(assetId, elementType);
        } else if (annotation.type() == Config.Type.Entity || Entity.class.isAssignableFrom(elementType)) {
            String entityId = (String) value;
            return entityManager.getEntity(entityId);
        } else if (elementType.isEnum()) {
            Method valueOf = elementType.getMethod("valueOf", String.class);
            return valueOf.invoke(null, value);
        } else {
            if (value instanceof Map && ((Map<?, ?>) value).containsKey("type")) {
                Map<String, Object> map = (Map<String, Object>) value;
                String typeName = (String) map.get("type");
                Object configValue = map.get("config");
                Class<?> type = Class.forName(typeName);
                return context.getLoaderManager().load(configValue, type, context);
            } else {
                return context.getLoaderManager().load(value, elementType, context);
            }
        }
    }

    private Object getConfig(LoadContext context, Class<?> elementType, Config annotation, Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();

        if (value == null) {
            return null;
        } else if (annotation.type() == Config.Type.Primitive || elementType.isPrimitive() || elementType == String.class) {
            return value;
        } else if (List.class.isAssignableFrom(elementType)) {
            List<Object> configList = new ArrayList<>();
            for (Object value1 : (List<Object>) value) {
                configList.add(getConfig(context, annotation.elementType(), annotation, value1));
            }
            return configList;
        } else if (annotation.type() == Config.Type.Asset) {
            return assetsManager.getId(elementType, value);
        } else if (annotation.type() == Config.Type.Entity || Entity.class.isAssignableFrom(elementType)) {
            return ((Entity) value).getId();
        } else if (elementType.isEnum()) {
            Method toString = elementType.getMethod("toString");
            return toString.invoke(value);
        } else {
            try {
                // Use LoaderManager <Object.class> to get config
                return context.getLoaderManager().getConfig(value, Object.class, context);
            } catch (RuntimeException e) {
                if (!(e.getMessage().startsWith("No such loader") || e.getMessage().startsWith("Can not get config")))
                    throw e;
                // Use LoaderManager <configType> to get config
                Class<?> type = value.getClass();
                return new LinkedHashMap<String, Object>() {{
                    put("type", type.getName());
                    put("config", context.getLoaderManager().getConfig(type.cast(value), Map.class, context));
                }};
            }
        }
    }

    private List<Field> getFields(Object obj) {
        List<Field> fields;
        fields = new ArrayList<>();
        Class<?> tmpType = obj.getClass();
        while (tmpType != null && tmpType != Object.class) {
            for (Field field : tmpType.getDeclaredFields()) {
                if (field.isAnnotationPresent(Config.class)) {
                    fields.add(field);
                }
            }
            tmpType = tmpType.getSuperclass();
        }
        return fields;
    }
}
