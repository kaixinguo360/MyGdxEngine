package com.my.world.core;

import com.my.world.core.util.Disposable;
import com.my.world.core.util.Pool;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

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
 *     listField:
 *        - type: com.my.com.customObject3
 *          config: ...
 *        - type: com.my.com.customObject4
 *          config: ...
 * </pre>
 */
public interface Configurable extends Disposable, Serializable {

    String CONTEXT_LAZY_LIST = "LAZY_LIST";

    interface OnLoad extends Configurable {
        void load(Map<String, Object> config, Context context);
    }

    interface OnInit extends Configurable {
        void init();
    }

    interface OnDump extends Configurable {
        Map<String, Object> dump(Context context);
    }

    static boolean load(Configurable configurable, Map<String, Object> config, Context context) {
        List<LazyContext> lazyList = context.get(CONTEXT_LAZY_LIST, List.class, null);
        LazyContext lazyContext = null;

        try {
            for (Field field : getFields(configurable)) {
                try {
                    field.setAccessible(true);
                    Config annotation = field.getAnnotation(Config.class);
                    String name = annotation.name();
                    if ("".equals(name)) name = field.getName();
                    Class<?> fieldType = field.getType();

                    if (annotation.fields().length == 0) {
                        if (!config.containsKey(name)) continue;

                        Object fieldConfig = config.get(name);

                        Object obj;
                        if (!List.class.isAssignableFrom(fieldType) && fieldType.isInstance(fieldConfig)) {
                            obj = fieldConfig;
                        } else {
                            try {
                                obj = getObject(context, fieldType, annotation, fieldConfig);
                            } catch (EntityManager.EntityManagerException e) {
                                if (lazyList == null) throw e;
                                if (lazyContext == null) {
                                    lazyContext = LazyContext.obtain(configurable, context);
                                    lazyList.add(lazyContext);
                                }
                                lazyContext.add(c -> {
                                    Object finalObj = getObject(c, fieldType, annotation, fieldConfig);
                                    setField(field, configurable, finalObj, c);
                                });
                                continue;
                            }
                        }

                        setField(field, configurable, obj, context);
                    } else {
                        Object fieldObject = field.get(configurable);

                        if (fieldObject == null) {
                            throw new RuntimeException("Can not set a null field: " + field);
                        }

                        for (String subFieldName : annotation.fields()) {
                            Field subField = fieldType.getField(subFieldName);
                            Class<?> subFieldType = subField.getType();
                            String subName = name + "." + subFieldName;
                            if (!config.containsKey(subName)) continue;
                            Object subFieldConfig = config.get(subName);

                            Object obj;
                            if (!List.class.isAssignableFrom(subFieldType) && subFieldType.isInstance(subFieldConfig)) {
                                obj = subFieldConfig;
                            } else {
                                try {
                                    obj = getObject(context, subFieldType, annotation, subFieldConfig);
                                } catch (EntityManager.EntityManagerException e) {
                                    if (lazyList == null) throw e;
                                    if (lazyContext == null) {
                                        lazyContext = LazyContext.obtain(configurable, context);
                                        lazyList.add(lazyContext);
                                    }
                                    lazyContext.add(c -> {
                                        Object finalObj = getObject(c, subFieldType, annotation, subFieldConfig);
                                        setField(subField, fieldObject, finalObj, c);
                                    });
                                    continue;
                                }
                            }

                            setField(subField, fieldObject, obj, context);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Load Field '" + field.getName() + "' (" + field.getType() + ") error: " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Load Loadable Resource(" + configurable.getClass() + ") error: " + e.getMessage(), e);
        }

        return lazyContext != null;
    }

    static void setField(Field field, Object target, Object value, Context context) throws IllegalAccessException {
        if (Modifier.isFinal(field.getModifiers())) {
            Object fieldObject = field.get(target);
            if (fieldObject == null) throw new RuntimeException("Can not set a final field: " + field);
            context.get(SerializerManager.CONTEXT_FIELD_NAME, SerializerManager.class).set(value, fieldObject);
        } else {
            field.set(target, value);
        }
    }

    static Map<String, Object> dump(Configurable configurable, Context context) {
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            for (Field field : getFields(configurable)) {
                try {
                    field.setAccessible(true);
                    Config annotation = field.getAnnotation(Config.class);
                    String name = annotation.name();
                    if ("".equals(name)) name = field.getName();

                    if (annotation.fields().length == 0) {
                        Object obj = getConfig(context, field.getType(), annotation, field.get(configurable));
                        map.put(name, obj);
                    } else {
                        Object fieldObject = field.get(configurable);

                        if (fieldObject == null) {
                            throw new RuntimeException("Can not dump a null field: " + field);
                        }

                        Class<?> fieldType = field.getType();
                        for (String subFieldName : annotation.fields()) {
                            Field subField = fieldType.getField(subFieldName);

                            Object obj = getConfig(context, subField.getType(), annotation, subField.get(fieldObject));
                            map.put(name + "." + subFieldName, obj);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Get config from Field '" + field.getName() + "' (" + field.getType() + ") error: " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Get config from Loadable Resource(" + configurable.getClass() + ") error: " + e.getMessage(), e);
        }
        return map;
    }

    static Object getObject(Context context, Class<?> elementType, Config annotation, Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        if (value == null) {
            return null;
        } else if (List.class.isAssignableFrom(elementType)) {
            List<Object> objList = new ArrayList<>();
            for (Object value1 : (List<Object>) value) {
                Class<?> listElementType = annotation.elementType();
                if (!List.class.isAssignableFrom(listElementType) && listElementType.isInstance(value1)) {
                    objList.add(value1);
                } else {
                    objList.add(getObject(context, listElementType, annotation, value1));
                }
            }
            return objList;
        } else if (annotation.type() == Config.Type.Primitive || elementType.isPrimitive() || Number.class.isAssignableFrom(elementType) || elementType == String.class || value.getClass().isPrimitive() || Number.class.isAssignableFrom(value.getClass())) {
            if ((elementType == float.class || elementType == Float.class || elementType == Object.class) && Number.class.isAssignableFrom(value.getClass())) {
                value = ((Number) value).floatValue();
            }
            return value;
        } else if (annotation.type() == Config.Type.Asset || Prefab.class.isAssignableFrom(elementType)) {
            String assetId = (String) value;
            AssetsManager assetsManager = context.get(AssetsManager.CONTEXT_FIELD_NAME, AssetsManager.class);
            return assetsManager.getAsset(assetId, elementType);
        } else if (annotation.type() == Config.Type.Entity || Entity.class.isAssignableFrom(elementType)) {
            String entityId = (String) value;
            Function<String, Entity> entityFinder = context.get(EntityUtil.CONTEXT_ENTITY_PROVIDER, Function.class);
            return entityFinder.apply(entityId);
        } else if (elementType.isEnum()) {
            Method valueOf = elementType.getMethod("valueOf", String.class);
            return valueOf.invoke(null, value);
        } else {
            if (value instanceof Map && ((Map<?, ?>) value).containsKey("type")) {
                Map<String, Object> map = (Map<String, Object>) value;
                String typeName = (String) map.get("type");
                Object configValue = map.get("config");
                Class<?> type = context.get(Engine.CONTEXT_FIELD_NAME, Engine.class).getJarManager().loadClass(typeName);
                return context.get(SerializerManager.CONTEXT_FIELD_NAME, SerializerManager.class).load(configValue, type, context);
            } else {
                return context.get(SerializerManager.CONTEXT_FIELD_NAME, SerializerManager.class).load(value, elementType, context);
            }
        }
    }

    static Object getConfig(Context context, Class<?> elementType, Config annotation, Object value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (value == null) {
            return null;
        } else if (annotation.type() == Config.Type.Primitive || elementType.isPrimitive() || Number.class.isAssignableFrom(elementType) || elementType == String.class || value.getClass().isPrimitive() || Number.class.isAssignableFrom(value.getClass())) {
            return value;
        } else if (List.class.isAssignableFrom(elementType)) {
            List<Object> configList = new ArrayList<>();
            for (Object value1 : (List<Object>) value) {
                configList.add(getConfig(context, annotation.elementType(), annotation, value1));
            }
            return configList;
        } else if (annotation.type() == Config.Type.Asset || Prefab.class.isAssignableFrom(elementType)) {
            AssetsManager assetsManager = context.get(AssetsManager.CONTEXT_FIELD_NAME, AssetsManager.class);
            return assetsManager.getId(elementType, value);
        } else if (annotation.type() == Config.Type.Entity || Entity.class.isAssignableFrom(elementType)) {
            return ((Entity) value).getId();
        } else if (elementType.isEnum()) {
            Method toString = elementType.getMethod("toString");
            return toString.invoke(value);
        } else {
            try {
                // Use SerializerManager <Object.class> to get config
                return context.get(SerializerManager.CONTEXT_FIELD_NAME, SerializerManager.class).dump(value, Object.class, context);
            } catch (RuntimeException e) {
                if (!(e.getMessage().startsWith("No such serializer") || e.getMessage().startsWith("Can not get config")))
                    throw e;
                // Use SerializerManager <configType> to get config
                Class<?> type = value.getClass();
                try {
                    return new LinkedHashMap<String, Object>() {{
                        put("type", type.getName());
                        put("config", context.get(SerializerManager.CONTEXT_FIELD_NAME, SerializerManager.class).dump(type.cast(value), Map.class, context));
                    }};
                } catch (RuntimeException exception) {
                    throw new RuntimeException("Error occurred while serialize this value: " + value + "(" + type.getName() + ")", exception);
                }
            }
        }
    }

    static List<Field> getFields(Object obj) {
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

    @Override
    default void dispose() {
        try {
            for (Field field : getFields(this)) {
                field.setAccessible(true);
                Class<?> type = field.getType();
                Config annotation = field.getAnnotation(Config.class);
                if (annotation.type() == Config.Type.Primitive || type.isPrimitive()) {
                    // Primitive Field
                    if (Modifier.isFinal(field.getModifiers())) throw new RuntimeException("Can not dispose a final field: " + field);
                    if (type == byte.class || type == char.class || type == short.class || type == int.class || type == long.class
                            || type == float.class || type == double.class) {
                        field.set(this, 0);
                    } else if (type == boolean.class) {
                        field.set(this, false);
                    }
                } else if (type == String.class || annotation.type() == Config.Type.Asset || annotation.type() == Config.Type.Entity
                        || Entity.class.isAssignableFrom(type) || Prefab.class.isAssignableFrom(type)) {
                    // Don't need to dispose
                    if (Modifier.isFinal(field.getModifiers())) throw new RuntimeException("Can not dispose a final field: " + field);
                    field.set(this, null);
                } else {
                    // Need to dispose
                    Object obj = field.get(this);
                    if (obj == null) continue;
                    if (obj instanceof List) {
                        Disposable.disposeAll((List<?>) obj);
                        if (!Modifier.isFinal(field.getModifiers())) field.set(this, null);
                    } else if (obj instanceof Collection) {
                        Disposable.disposeAll((Collection<?>) obj);
                        if (!Modifier.isFinal(field.getModifiers())) field.set(this, null);
                    } else if (obj instanceof Map) {
                        Disposable.disposeAll((Map<?, ?>) obj);
                        if (!Modifier.isFinal(field.getModifiers())) field.set(this, null);
                    } else {
                        Disposable.dispose(obj);
                        if (Modifier.isFinal(field.getModifiers())) field.set(this, null);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Dispose Resource(" + this.getClass() + ") error: " + e.getMessage(), e);
        }
    }

    class LazyContext implements Disposable {

        private Object configurable;
        private Context context;
        private final List<LazyLoadFunction> functions = new ArrayList<>();

        public void add(LazyLoadFunction function) {
            functions.add(function);
        }

        public void load() {
            for (LazyLoadFunction function : functions) {
                try {
                    function.run(context);
                } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
                    throw new RuntimeException("Load Loadable Resource(" + configurable.getClass() + ") error: " + e.getMessage(), e);
                }
            }

            if (configurable instanceof Configurable.OnInit) {
                ((Configurable.OnInit) configurable).init();
            }
        }

        @Override
        public void dispose() {
            context.dispose();

            this.configurable = null;
            this.context = null;
            this.functions.clear();

            pool.free(this);
        }

        private static final Pool<LazyContext> pool = new Pool<>(LazyContext::new);
        public static LazyContext obtain(Object configurable, Context context) {
            LazyContext lazyContext = pool.obtain();

            lazyContext.configurable = configurable;
            lazyContext.context = context.clone();

            return lazyContext;
        }
    }

    @FunctionalInterface
    interface LazyLoadFunction {
        void run(Context context) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException;
    }
}
