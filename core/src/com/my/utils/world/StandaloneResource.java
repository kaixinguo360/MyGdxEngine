package com.my.utils.world;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Config Example:
 * <pre>
 *     intField: 1
 *     floatField: 2.0
 *     stringField: "string"
 *     customField1:
 *          type: com.my.com.customObject1
 *          config: ...
 *     customField2:
 *          type: com.my.com.customObject2
 *          config: ...
 * </pre>
 */
public interface StandaloneResource extends Loadable<Map<String, Object>> {

    default void load(Map<String, Object> config, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        try {
            Field[] fields = this.getClass().getFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Config.class)) {
                    Config annotation = field.getAnnotation(Config.class);
                    String name = annotation.name();
                    if ("".equals(name)) name = field.getName();
                    if (annotation.type() == Config.Type.Primitive || field.getType().isPrimitive() || field.getType() == String.class || config.get(name) == null) {
                        field.set(this, config.get(name));
                    } else if (annotation.type() == Config.Type.Asset) {
                        String assetId = (String) config.get(name);
                        Object obj = assetsManager.getAsset(assetId, field.getType());
                        field.set(this, obj);
                    } else if (field.getType().isEnum()) {
                        Method toString = field.getType().getMethod("valueOf", String.class);
                        Object obj = toString.invoke(null, config.get(name));
                        field.set(this, obj);
                    } else {
                        try {
                            // Use LoaderManager <Object.class> to load field
                            Object obj = context.getLoaderManager().load(config.get(name), field.getType(), context);
                            field.set(this, obj);
                        } catch (RuntimeException e) {
                            if (!(e.getMessage().startsWith("No such loader") || e.getMessage().startsWith("Can not load"))) throw e;
                            // Use LoaderManager <configType> to load field
                            Map<String, Object> map = (Map<String, Object>) config.get(name);
                            String typeName = (String) map.get("type");
                            Object configValue = map.get("config");
                            Class<?> type = Class.forName(typeName);
                            Object obj = context.getLoaderManager().load(configValue, type, context);
                            field.set(this, obj);
                        }
                    }
                }
            }
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Load StandaloneResource(" + this.getClass() + ") error: " + e.getMessage(), e);
        }
    }

    default Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            Field[] fields = this.getClass().getFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Config.class)) {
                    Config annotation = field.getAnnotation(Config.class);
                    String name = annotation.name();
                    if ("".equals(name)) name = field.getName();
                    Object obj = field.get(this);
                    if (annotation.type() == Config.Type.Primitive || field.getType().isPrimitive() || field.getType() == String.class || obj == null) {
                        map.put(name, obj);
                    } else if (annotation.type() == Config.Type.Asset) {
                        String assetId = assetsManager.getId(field.getType(), field.get(this));
                        map.put(name, assetId);
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
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Get config from StandaloneResource(" + this.getClass() + ") error: " + e.getMessage(), e);
        }
        return map;
    }

    @Override
    default <E> boolean handleable(Class<E> configType) {
        return Map.class.isAssignableFrom(configType);
    }
}
