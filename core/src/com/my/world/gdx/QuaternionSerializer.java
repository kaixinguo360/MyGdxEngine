package com.my.world.gdx;

import com.badlogic.gdx.math.Quaternion;
import com.my.world.core.Context;
import com.my.world.core.Serializer;

import java.util.ArrayList;
import java.util.List;

public class QuaternionSerializer implements Serializer, Serializer.Setter {

    @Override
    public <E, T> T load(E config, Class<T> type, Context context) {
        List<Number> values = (List<Number>) config;
        return (T) new Quaternion(
                values.get(0).floatValue(),
                values.get(1).floatValue(),
                values.get(2).floatValue(),
                values.get(3).floatValue()
        );
    }

    @Override
    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        Quaternion quaternion = (Quaternion) obj;
        return (E) new ArrayList<Number>() {{
            add(quaternion.x);
            add(quaternion.y);
            add(quaternion.z);
            add(quaternion.w);
        }};
    }

    @Override
    public <E, T> boolean canSerialize(Class<E> configType, Class<T> targetType) {
        return (configType == Object.class || List.class.isAssignableFrom(configType)) && targetType == Quaternion.class;
    }

    @Override
    public void set(Object sourceObj, Object targetObj) {
        Quaternion source = (Quaternion) sourceObj;
        Quaternion target = (Quaternion) targetObj;
        target.set(source);
    }

    @Override
    public boolean canSet(Class<?> sourceType, Class<?> targetType) {
        return Quaternion.class.isAssignableFrom(sourceType) && Quaternion.class.isAssignableFrom(targetType);
    }
}
