package com.my.demo.builder;

import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;

import java.util.Map;

public interface EntityBuilder {

    EntityBuilder init(Engine engine, Scene scene) throws DependenciesException;

    Entity build(Scene scene, Map<String, Object> params);

    default Entity build(Scene scene) {
        return build(scene, null);
    }

    class DependenciesException extends RuntimeException {
        public DependenciesException(Class<? extends EntityBuilder> builderType) {
            super("No such builder: " + builderType);
        }
    }
}
