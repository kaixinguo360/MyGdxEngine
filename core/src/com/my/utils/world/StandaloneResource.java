package com.my.utils.world;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public interface StandaloneResource extends Loadable<Map<String, Object>> {

    default void load(Map<String, Object> config, LoadContext context) {
        try {
            Field[] fields = this.getClass().getFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Config.class)) {
                    String name = field.getName();
                    Class<?> type = field.getType();
                    Object configValue = config.get(name);
                    if (type.isPrimitive()) {
                        field.set(this, configValue);
                    } else {
                        field.set(this, context.getLoaderManager().load(configValue, type));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("StandaloneResource(" + this.getClass() + ") getConfig error: " + e.getMessage(), e);
        }
    }

    default Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            Field[] fields = this.getClass().getFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Config.class)) {
                    Config annotation = field.getAnnotation(Config.class);
                    String name = field.getName();
                    Object obj = field.get(this);
                    Class<?> type = field.getType();
                    if (annotation.isPrimitive() || type.isPrimitive()) {
                        map.put(name, obj);
                    } else {
                        map.put(name, context.getLoaderManager().getConfig(type.cast(obj), configType));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("StandaloneResource(" + this.getClass() + ") getConfig error: " + e.getMessage(), e);
        }
        return map;
    }

    @Override
    default <E> boolean handleable(Class<E> configType) {
        return Map.class.isAssignableFrom(configType);
    }
}
