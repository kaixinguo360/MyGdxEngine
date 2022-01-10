package com.my.world.module.render.light;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.module.render.Light;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DirectionalLight extends Light {

    @Config(fields = { "color", "direction" })
    private final com.badlogic.gdx.graphics.g3d.environment.DirectionalLight light = new com.badlogic.gdx.graphics.g3d.environment.DirectionalLight();

    public DirectionalLight(Color color, Vector3 direction) {
        light.set(color, direction);
    }

    @Override
    public BaseLight getLight() {
        return light;
    }
}
