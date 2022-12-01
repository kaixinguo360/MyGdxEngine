package com.my.world.enhanced.builder;

import com.my.world.core.Configurable;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;

import java.util.Map;

public interface EntityBuilder extends Configurable {

    EntityBuilder init(Engine engine, Scene scene) throws DependenciesException;

    Entity build(Scene scene, Map<String, Object> params);

    default Entity build(Scene scene) {
        return build(scene, null);
    }

}
