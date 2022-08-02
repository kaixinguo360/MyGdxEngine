package com.my.demo.builder;

import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.util.Pool;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class ParamBuilder<T extends ParamBuilder<T, P>, P> extends BaseBuilder<T> {

    public Pool<P> pool = new Pool<>(this::newParam);

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        P p = mapToParam(params, pool.obtain());
        Entity entity = build(scene, p);
        pool.free(p);
        return entity;
    }

    // ----- Abstract Method ----- //

    public abstract Entity build(Scene scene, P p);

    public abstract P newParam();

    // ----- Util Method ----- //

    public static <T> T mapToParam(Map<String, Object> params, T param) {
        try {
            if (params == null) return param;
            List<Field> fields = getFields(param);
            for (Field field : fields) {
                String name = field.getName();
                if (!params.containsKey(name)) continue;
                Object value = params.get(name);
                if (value != null) {
                    Class<?> fieldType = field.getType();
                    if (value instanceof Number) {
                        if (fieldType == float.class) {
                            value = ((Number) value).floatValue();
                        } else if (fieldType == double.class) {
                            value = ((Number) value).doubleValue();
                        } else if (fieldType == byte.class) {
                            value = ((Number) value).byteValue();
                        } else if (fieldType == short.class) {
                            value = ((Number) value).shortValue();
                        } else if (fieldType == int.class) {
                            value = ((Number) value).intValue();
                        } else if (fieldType == long.class) {
                            value = ((Number) value).longValue();
                        }
                    } else if (value instanceof Boolean) {
                        if (fieldType == boolean.class) {
                            value = (boolean) value;
                        }
                    } else {
                        Class<?> valueClass = value.getClass();
                        if (!fieldType.isAssignableFrom(valueClass)) {
                            throw new RuntimeException("Can not set value to this field: " + value + "(" + valueClass.getName() + ") -> "
                                    + param.getClass().getName() + "#" + field.getName() + "(" + fieldType.getName() + ")");
                        }
                    }
                }
                field.setAccessible(true);
                field.set(param, value);
            }
            return param;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Field> getFields(Object obj) {
        List<Field> fields;
        fields = new ArrayList<>();
        Class<?> tmpType = obj.getClass();
        while (tmpType != null && tmpType != Object.class) {
            fields.addAll(Arrays.asList(tmpType.getDeclaredFields()));
            tmpType = tmpType.getSuperclass();
        }
        return fields;
    }
}
