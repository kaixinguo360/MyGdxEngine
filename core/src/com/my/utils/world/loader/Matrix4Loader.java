package com.my.utils.world.loader;

import com.badlogic.gdx.math.Matrix4;
import com.my.utils.world.LoadContext;
import com.my.utils.world.Loader;

import java.util.ArrayList;
import java.util.List;

public class Matrix4Loader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        List<Number> list = (List<Number>) config;
        float[] values = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            values[i] = list.get(i).floatValue();
        }
        return (T) new Matrix4(values);
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
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
}
