package com.my.utils.world;

import java.lang.reflect.Field;
import java.util.HashMap;
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
        try {
            Field[] fields = this.getClass().getFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Config.class)) {
                    Config annotation = field.getAnnotation(Config.class);
                    String name = annotation.name();
                    if ("".equals(name)) name = field.getName();
                    if (annotation.isPrimitive() || config.get(name) == null) {
                        field.set(this, config.get(name));
                    } else {
                        // Use LoaderManager to load field
                        Map<String, Object> map = (Map<String, Object>) config.get(name);
                        String typeName = (String) map.get("type");
                        Object configValue = map.get("config");
                        Class<?> type = Class.forName(typeName);
                        Object obj = context.getLoaderManager().load(configValue, type, context);
                        field.set(this, obj);
                    }
                }
            }
        } catch (IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("Load StandaloneResource(" + this.getClass() + ") error: " + e.getMessage(), e);
        }
    }

    default Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            Field[] fields = this.getClass().getFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Config.class)) {
                    Config annotation = field.getAnnotation(Config.class);
                    String name = annotation.name();
                    if ("".equals(name)) name = field.getName();
                    Object obj = field.get(this);
                    if (annotation.isPrimitive() || obj == null) {
                        map.put(name, obj);
                    } else {
                        // Use LoaderManager to get config
                        Class<?> type = obj.getClass();
                        map.put(name, new HashMap<String, Object>() {{
                            put("type", type.getName());
                            put("config", context.getLoaderManager().getConfig(type.cast(obj), configType, context));
                        }});
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Get config from StandaloneResource(" + this.getClass() + ") error: " + e.getMessage(), e);
        }
        return map;
    }

    @Override
    default <E> boolean handleable(Class<E> configType) {
        return Map.class.isAssignableFrom(configType);
    }
}
