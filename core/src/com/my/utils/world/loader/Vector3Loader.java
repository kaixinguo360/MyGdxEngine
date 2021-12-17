package com.my.utils.world.loader;

import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.LoadContext;
import com.my.utils.world.Loader;

import java.util.ArrayList;
import java.util.List;

public class Vector3Loader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        List<Float> values = (List<Float>) config;
        return (T) new Vector3(values.get(0), values.get(1), values.get(2));
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
        Vector3 vector3 = (Vector3) obj;
        return (E) new ArrayList<Float>() {{
            add(vector3.x);
            add(vector3.y);
            add(vector3.z);
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (configType == Object.class || List.class.isAssignableFrom(configType)) && targetType == Vector3.class;
    }
}
