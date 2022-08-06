package com.my.world.enhanced.builder;

import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.entity.EnhancedEntity;

import java.util.Map;

public interface EntityGenerator extends EntityBuilder {

    EnhancedEntity generate();

    default Entity build(Scene scene, Map<String, Object> params) {
        EnhancedEntity entity = generate();
        entity.addToScene(scene);
        return entity;
    };

    default EntityBuilder init(Engine engine, Scene scene) throws DependenciesException {
        return this;
    };
}
