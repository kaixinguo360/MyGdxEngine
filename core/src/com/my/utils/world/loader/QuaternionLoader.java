package com.my.utils.world.loader;

import com.badlogic.gdx.math.Quaternion;
import com.my.utils.world.LoadContext;
import com.my.utils.world.Loader;

import java.util.ArrayList;
import java.util.List;

public class QuaternionLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        List<Float> values = (List<Float>) config;
        return (T) new Quaternion(values.get(0), values.get(1), values.get(2), values.get(3));
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
        Quaternion quaternion = (Quaternion) obj;
        return (E) new ArrayList<Float>() {{
            add(quaternion.x);
            add(quaternion.y);
            add(quaternion.z);
            add(quaternion.w);
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (configType == Object.class || List.class.isAssignableFrom(configType)) && targetType == Quaternion.class;
    }
}
