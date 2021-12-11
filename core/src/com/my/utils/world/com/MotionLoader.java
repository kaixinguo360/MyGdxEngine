package com.my.utils.world.com;

import com.my.utils.world.Loader;

import java.util.HashMap;
import java.util.Map;

public class MotionLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type) {
        Map<String, Object> map = (Map<String, Object>) config;
        return (T) new Motion(
                (String) map.get("type"),
                (Map<String, Object>) map.get("config")
        );
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType) {
        Motion motion = (Motion) obj;
        return (E) new HashMap<String, Object>() {{
            put("type", motion.getType());
            put("config", motion.getConfig());
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == Motion.class);
    }
}
