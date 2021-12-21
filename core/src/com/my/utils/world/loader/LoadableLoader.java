package com.my.utils.world.loader;

import com.my.utils.world.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoadableLoader implements Loader {

    @Override
    public <E, T> T load(E configObj, Class<T> typeA, LoadContext context) {
        World world = context.getEnvironment("world", World.class);
        EntityManager entityManager = world.getEntityManager();
        AssetsManager assetsManager = world.getAssetsManager();
        Map<String, Object> config = (Map<String, Object>) configObj;

        try {
            Loadable loadable = (Loadable) typeA.newInstance();
            if (loadable instanceof Loadable.OnLoad) {
                ((Loadable.OnLoad) loadable).load(config, context);
            } else {
                List<Field> fields = getFields(loadable);
                for (Field field : fields) {
                    field.setAccessible(true);
                    Config annotation = field.getAnnotation(Config.class);
                    String name = annotation.name();
                    if ("".equals(name)) name = field.getName();
                    if (config.get(name) == null) {
                        field.set(loadable, config.get(name));
                    } else if (List.class.isAssignableFrom(field.getType())) {
                        Class<?> elementType = annotation.elementType();
                        List<Object> list = (List<Object>) config.get(name);
                        List<Object> objList = new ArrayList<>();
                        for (Object value : list) {
                            if (value == null) {
                                objList.add(value);
                            } else if (annotation.type() == Config.Type.Primitive || elementType.isPrimitive() || elementType == String.class) {
                                if ((elementType == float.class || elementType == Float.class) && Number.class.isAssignableFrom(value.getClass())) {
                                    value = ((Number) value).floatValue();
                                }
                                objList.add(value);
                            } else if (annotation.type() == Config.Type.Asset) {
                                String assetId = (String) value;
                                Object obj = assetsManager.getAsset(assetId, elementType);
                                objList.add(obj);
                            } else if (annotation.type() == Config.Type.Entity || Entity.class.isAssignableFrom(elementType)) {
                                String entityId = (String) value;
                                Entity obj = entityManager.getEntity(entityId);
                                objList.add(obj);
                            } else if (elementType.isEnum()) {
                                Method valueOf = elementType.getMethod("valueOf", String.class);
                                Object obj = valueOf.invoke(null, value);
                                objList.add(obj);
                            } else {
                                if (value instanceof Map && ((Map<?, ?>) value).containsKey("type")) {
                                    Map<String, Object> map = (Map<String, Object>) value;
                                    String typeName = (String) map.get("type");
                                    Object configValue = map.get("config");
                                    Class<?> type = Class.forName(typeName);
                                    Object obj = context.getLoaderManager().load(configValue, type, context);
                                    objList.add(obj);
                                } else {
                                    Object obj = context.getLoaderManager().load(value, elementType, context);
                                    objList.add(obj);
                                }
                            }
                        }
                        if (Modifier.isFinal(field.getModifiers())) {
                            if (field.get(loadable) == null) throw new RuntimeException("Can not set a final field: " + field);
                            List<Object> fieldList = (List<Object>) field.get(loadable);
                            fieldList.clear();
                            fieldList.addAll(objList);
                        } else {
                            field.set(loadable, objList);
                        }
                    } else if (annotation.type() == Config.Type.Primitive || field.getType().isPrimitive() || field.getType() == String.class) {
                        Object value = config.get(name);
                        if ((field.getType() == float.class || field.getType() == Float.class) && Number.class.isAssignableFrom(value.getClass())) {
                            value = ((Number) value).floatValue();
                        }
                        field.set(loadable, value);
                    } else if (annotation.type() == Config.Type.Asset) {
                        String assetId = (String) config.get(name);
                        Object obj = assetsManager.getAsset(assetId, field.getType());
                        field.set(loadable, obj);
                    } else if (annotation.type() == Config.Type.Entity || Entity.class.isAssignableFrom(field.getType())) {
                        String entityId = (String) config.get(name);
                        Entity obj = entityManager.getEntity(entityId);
                        field.set(loadable, obj);
                    } else if (field.getType().isEnum()) {
                        Method valueOf = field.getType().getMethod("valueOf", String.class);
                        Object obj = valueOf.invoke(null, config.get(name));
                        field.set(loadable, obj);
                    } else {
                        if (config.get(name) instanceof Map && ((Map<?, ?>) config.get(name)).containsKey("type")) {
                            Map<String, Object> map = (Map<String, Object>) config.get(name);
                            String typeName = (String) map.get("type");
                            Object configValue = map.get("config");
                            Class<?> type = Class.forName(typeName);
                            Object obj = context.getLoaderManager().load(configValue, type, context);
                            field.set(loadable, obj);
                        } else {
                            Object obj = context.getLoaderManager().load(config.get(name), field.getType(), context);
                            field.set(loadable, obj);
                        }
                    }
                }
            }
            if (loadable instanceof Loadable.OnInit) {
                ((Loadable.OnInit) loadable).init();
            }
            return (T) loadable;
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException("Load Loadable Resource(" + typeA + ") error: " + e.getMessage(), e);
        }
    }

    @Override
    public <E, T> E getConfig(T loadable, Class<E> configType, LoadContext context) {
        World world = context.getEnvironment("world", World.class);
        EntityManager entityManager = world.getEntityManager();
        AssetsManager assetsManager = world.getAssetsManager();

        try {
            if (loadable instanceof Loadable.OnGetConfig) {
                return (E) ((Loadable.OnGetConfig) loadable).getConfig(context);
            } else {
                Map<String, Object> map = new LinkedHashMap<>();
                List<Field> fields;
                fields = getFields(loadable);
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Config.class)) {
                        Config annotation = field.getAnnotation(Config.class);
                        String name = annotation.name();
                        if ("".equals(name)) name = field.getName();
                        Object obj = field.get(loadable);
                        if (obj == null) {
                            map.put(name, obj);
                        } else if (List.class.isAssignableFrom(field.getType())) {
                            Class<?> elementType = annotation.elementType();
                            List<Object> list = (List<Object>) field.get(loadable);
                            List<Object> configList = new ArrayList<>();
                            for (Object value : list) {
                                if (annotation.type() == Config.Type.Primitive || elementType.isPrimitive() || elementType == String.class) {
                                    configList.add(value);
                                } else if (annotation.type() == Config.Type.Asset) {
                                    String assetId = assetsManager.getId(elementType, value);
                                    configList.add(assetId);
                                } else if (annotation.type() == Config.Type.Entity || Entity.class.isAssignableFrom(elementType)) {
                                    String entityId = ((Entity) value).getId();
                                    configList.add(entityId);
                                } else if (elementType.isEnum()) {
                                    Method toString = elementType.getMethod("toString");
                                    configList.add(toString.invoke(value));
                                } else {
                                    try {
                                        // Use LoaderManager <Object.class> to get config
                                        configList.add(context.getLoaderManager().getConfig(value, Object.class, context));
                                    } catch (RuntimeException e) {
                                        if (!(e.getMessage().startsWith("No such loader") || e.getMessage().startsWith("Can not get config")))
                                            throw e;
                                        // Use LoaderManager <configType> to get config
                                        Class<?> type = value.getClass();
                                        configList.add(new LinkedHashMap<String, Object>() {{
                                            put("type", type.getName());
                                            put("config", context.getLoaderManager().getConfig(type.cast(value), configType, context));
                                        }});
                                    }
                                }
                            }
                            map.put(name, configList);
                        } else if (annotation.type() == Config.Type.Primitive || field.getType().isPrimitive() || field.getType() == String.class) {
                            map.put(name, obj);
                        } else if (annotation.type() == Config.Type.Asset) {
                            String assetId = assetsManager.getId(field.getType(), field.get(loadable));
                            map.put(name, assetId);
                        } else if (annotation.type() == Config.Type.Entity || Entity.class.isAssignableFrom(field.getType())) {
                            String entityId = ((Entity) field.get(loadable)).getId();
                            map.put(name, entityId);
                        } else if (field.getType().isEnum()) {
                            Method toString = field.getType().getMethod("toString");
                            map.put(name, toString.invoke(obj));
                        } else {
                            try {
                                // Use LoaderManager <Object.class> to get config
                                map.put(name, context.getLoaderManager().getConfig(obj, Object.class, context));
                            } catch (RuntimeException e) {
                                if (!(e.getMessage().startsWith("No such loader") || e.getMessage().startsWith("Can not get config"))) throw e;
                                // Use LoaderManager <configType> to get config
                                Class<?> type = obj.getClass();
                                map.put(name, new LinkedHashMap<String, Object>() {{
                                    put("type", type.getName());
                                    put("config", context.getLoaderManager().getConfig(type.cast(obj), configType, context));
                                }});
                            }
                        }
                    }
                }
                return (E) map;
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Get config from Loadable Resource(" + loadable.getClass() + ") error: " + e.getMessage(), e);
        }
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return Map.class.isAssignableFrom(configType) && Loadable.class.isAssignableFrom(targetType);
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
