package com.my.utils.world;

import java.util.Map;

public interface LoadableResource extends Loadable<Map<String, Object>> {
    @Override
    default <E> boolean handleable(Class<E> configType) {
        return Map.class.isAssignableFrom(configType);
    }
}
