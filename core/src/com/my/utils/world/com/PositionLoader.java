package com.my.utils.world.com;

import com.badlogic.gdx.math.Matrix4;
import com.my.utils.world.LoadContext;
import com.my.utils.world.Loader;

import java.util.HashMap;
import java.util.Map;

public class PositionLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        Map<String, Double> map = (Map<String, Double>) config;
        float[] values = new float[16];
        for (int i = 0; i < values.length; i++) {
            values[i] = (float) (double) map.get("v" + i);
        }
        Matrix4 matrix4 = new Matrix4(values);
        return (T) new Position(matrix4);
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
        Position position = (Position) obj;
        float[] values = position.transform.getValues();
        return (E) new HashMap<String, Double>() {{
            for (int i = 0; i < values.length; i++) {
                put("v" + i, (double) values[i]);
            }
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == Position.class);
    }
}
