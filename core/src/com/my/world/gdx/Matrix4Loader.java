package com.my.world.gdx;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Context;
import com.my.world.core.Loader;

import java.util.ArrayList;
import java.util.List;

public class Matrix4Loader implements Loader, Loader.Setter {

    @Override
    public <E, T> T load(E config, Class<T> type, Context context) {
        List<Number> list = (List<Number>) config;
        float[] values = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            values[i] = list.get(i).floatValue();
        }
        return (T) new Matrix4(values);
    }

    @Override
    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        Matrix4 transform = (Matrix4) obj;
        return (E) new ArrayList<Number>() {{
            for (float v : transform.val) {
                add(v);
            }
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (configType == Object.class || List.class.isAssignableFrom(configType)) && targetType == Matrix4.class;
    }

    @Override
    public void set(Object sourceObj, Object targetObj) {
        Matrix4 source = (Matrix4) sourceObj;
        Matrix4 target = (Matrix4) targetObj;
        target.set(source);
    }

    @Override
    public boolean setterHandleable(Class<?> sourceType, Class<?> targetType) {
        return Matrix4.class.isAssignableFrom(sourceType) && Matrix4.class.isAssignableFrom(targetType);
    }
}
