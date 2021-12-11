package com.my.utils.world.com;

import com.my.utils.world.Loader;

import java.util.HashMap;
import java.util.Map;

public class CollisionLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type) {
        Map<String, Object> map = (Map<String, Object>) config;
        return (T) new Collision(
                (int) map.get("callbackFilter"),
                (int) map.get("callbackFlag"),
                (String) map.get("handlerName")
        );
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType) {
        Collision collision = (Collision) obj;
        return (E) new HashMap<String, Object>() {{
            put("callbackFilter", collision.getCallbackFilter());
            put("callbackFlag", collision.getCallbackFlag());
            put("handlerName", collision.getHandlerName());
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == Collision.class);
    }
}
