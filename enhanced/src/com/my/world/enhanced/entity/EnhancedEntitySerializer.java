package com.my.world.enhanced.entity;

import com.my.world.core.*;

import java.util.Map;

public class EnhancedEntitySerializer extends ConfigurableSerializer {

    @Override
    protected <E, T> Configurable newInstance(E configObj, Class<T> type, Context context) {
        return new Entity();
    }

    @Override
    public <E, T> boolean canSerialize(Class<E> configType, Class<T> targetType) {
        return Map.class.isAssignableFrom(configType) && (EnhancedEntity.class.isAssignableFrom(targetType) || targetType == EnhancedEntity.class);
    }
}
