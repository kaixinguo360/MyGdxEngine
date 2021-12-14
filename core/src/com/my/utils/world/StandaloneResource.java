package com.my.utils.world;

import java.util.HashMap;
import java.util.Map;

public interface StandaloneResource extends Loadable<Map<String, Object>> {

    default void load(Map<String, Object> config, LoadContext context) {
    }

    default Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        return new HashMap<>();
    }

    @Override
    default <E> boolean handleable(Class<E> configType) {
        return Map.class.isAssignableFrom(configType);
    }
}
