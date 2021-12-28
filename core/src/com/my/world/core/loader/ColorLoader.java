package com.my.world.core.loader;

import com.badlogic.gdx.graphics.Color;
import com.my.world.core.Context;
import com.my.world.core.Loader;

import java.util.ArrayList;
import java.util.List;

public class ColorLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, Context context) {
        List<Number> values = (List<Number>) config;
        return (T) new Color(
                values.get(0).floatValue(),
                values.get(1).floatValue(),
                values.get(2).floatValue(),
                values.get(3).floatValue()
        );
    }

    @Override
    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        Color color = (Color) obj;
        return (E) new ArrayList<Number>() {{
            add(color.r);
            add(color.g);
            add(color.b);
            add(color.a);
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (configType == Object.class || List.class.isAssignableFrom(configType)) && targetType == Color.class;
    }
}
