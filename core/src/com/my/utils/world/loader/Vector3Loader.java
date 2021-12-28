package com.my.utils.world.loader;

import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Context;
import com.my.utils.world.Loader;

import java.util.ArrayList;
import java.util.List;

public class Vector3Loader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, Context context) {
        List<Number> values = (List<Number>) config;
        return (T) new Vector3(
                values.get(0).floatValue(),
                values.get(1).floatValue(),
                values.get(2).floatValue()
        );
    }

    @Override
    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        Vector3 vector3 = (Vector3) obj;
        return (E) new ArrayList<Number>() {{
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
