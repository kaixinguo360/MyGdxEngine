package com.my.world.module.render.light;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import com.my.world.module.render.Light;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DirectionalLight extends Light implements Loadable.OnInit {

    @Config public Color color;
    @Config public Vector3 direction;

    private com.badlogic.gdx.graphics.g3d.environment.DirectionalLight light;

    public DirectionalLight(Color color, Vector3 direction) {
        this.color = color;
        this.direction = direction;
        init();
    }

    @Override
    public void init() {
        light = new com.badlogic.gdx.graphics.g3d.environment.DirectionalLight();
        light.set(color, direction);
    }

    @Override
    public BaseLight getLight() {
        return light;
    }
}
