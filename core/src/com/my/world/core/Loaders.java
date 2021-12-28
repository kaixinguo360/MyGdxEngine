package com.my.world.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class Loaders {

    public static void load(Loadable loadable, Map<String, Object> config, Context context) {
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
            throw new RuntimeException("Load Loadable Resource(" + loadable.getClass() + ") error: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> dump(Loadable loadable, Context context) {
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
        return map;
    }

    private static Object getObject(Context context, Class<?> elementType, Config annotation, Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
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
        } else if (annotation.type() == Config.Type.Asset || Prefab.class.isAssignableFrom(elementType)) {
            String assetId = (String) value;
            AssetsManager assetsManager = context.getEnvironment(AssetsManager.CONTEXT_FIELD_NAME, AssetsManager.class);
            return assetsManager.getAsset(assetId, elementType);
        } else if (annotation.type() == Config.Type.Entity || Entity.class.isAssignableFrom(elementType)) {
            String entityId = (String) value;
            EntityManager entityManager = context.getEnvironment(EntityManager.CONTEXT_FIELD_NAME, EntityManager.class);
            return entityManager.findEntityById(entityId);
        } else if (elementType.isEnum()) {
            Method valueOf = elementType.getMethod("valueOf", String.class);
            return valueOf.invoke(null, value);
        } else {
            if (value instanceof Map && ((Map<?, ?>) value).containsKey("type")) {
                Map<String, Object> map = (Map<String, Object>) value;
                String typeName = (String) map.get("type");
                Object configValue = map.get("config");
                Class<?> type = Class.forName(typeName);
                return context.getEnvironment(LoaderManager.CONTEXT_FIELD_NAME, LoaderManager.class).load(configValue, type, context);
            } else {
                return context.getEnvironment(LoaderManager.CONTEXT_FIELD_NAME, LoaderManager.class).load(value, elementType, context);
            }
        }
    }

    private static Object getConfig(Context context, Class<?> elementType, Config annotation, Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
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
        } else if (annotation.type() == Config.Type.Asset || Prefab.class.isAssignableFrom(elementType)) {
            AssetsManager assetsManager = context.getEnvironment(AssetsManager.CONTEXT_FIELD_NAME, AssetsManager.class);
            return assetsManager.getId(elementType, value);
        } else if (annotation.type() == Config.Type.Entity || Entity.class.isAssignableFrom(elementType)) {
            return ((Entity) value).getId();
        } else if (elementType.isEnum()) {
            Method toString = elementType.getMethod("toString");
            return toString.invoke(value);
        } else {
            try {
                // Use LoaderManager <Object.class> to get config
                return context.getEnvironment(LoaderManager.CONTEXT_FIELD_NAME, LoaderManager.class).dump(value, Object.class, context);
            } catch (RuntimeException e) {
                if (!(e.getMessage().startsWith("No such loader") || e.getMessage().startsWith("Can not get config")))
                    throw e;
                // Use LoaderManager <configType> to get config
                Class<?> type = value.getClass();
                return new LinkedHashMap<String, Object>() {{
                    put("type", type.getName());
                    put("config", context.getEnvironment(LoaderManager.CONTEXT_FIELD_NAME, LoaderManager.class).dump(type.cast(value), Map.class, context));
                }};
            }
        }
    }

    public static List<Field> getFields(Object obj) {
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
