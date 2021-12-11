package com.my.utils.world.com;

import com.my.utils.world.Loader;

import java.util.HashMap;
import java.util.Map;

public class SerializationLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type) {
        Map<String, Object> map = (Map<String, Object>) config;
        return (T) new Serialization(
                (String) map.get("group"),
                (String) map.get("serializerId")
        );
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType) {
        Serialization serialization = (Serialization) obj;
        return (E) new HashMap<String, Object>() {{
            put("group", serialization.getGroup());
            put("serializerId", serialization.getSerializerId());
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == Serialization.class);
    }
}
