package com.my.world.gdx;

import com.badlogic.gdx.math.Vector2;
import com.my.world.core.Context;
import com.my.world.core.Serializer;

import java.util.ArrayList;
import java.util.List;

public class Vector2Serializer implements Serializer, Serializer.Setter {

    @Override
    public <E, T> T load(E config, Class<T> type, Context context) {
        List<Number> values = (List<Number>) config;
        return (T) new Vector2(
                values.get(0).floatValue(),
                values.get(1).floatValue()
        );
    }

    @Override
    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        Vector2 Vector2 = (Vector2) obj;
        return (E) new ArrayList<Number>() {{
            add(Vector2.x);
            add(Vector2.y);
        }};
    }

    @Override
    public <E, T> boolean canSerialize(Class<E> configType, Class<T> targetType) {
        return (configType == Object.class || List.class.isAssignableFrom(configType)) && targetType == Vector2.class;
    }

    @Override
    public void set(Object sourceObj, Object targetObj) {
        Vector2 source = (Vector2) sourceObj;
        Vector2 target = (Vector2) targetObj;
        target.set(source);
    }

    @Override
    public boolean canSet(Class<?> sourceType, Class<?> targetType) {
        return Vector2.class.isAssignableFrom(sourceType) && Vector2.class.isAssignableFrom(targetType);
    }
}
