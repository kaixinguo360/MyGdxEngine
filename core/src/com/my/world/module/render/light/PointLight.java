package com.my.world.module.render.light;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.render.Light;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PointLight extends Light implements Loadable.OnInit {

    @Config public Color color;
    @Config public Vector3 rel_pos;
    @Config public float intensity;

    private com.badlogic.gdx.graphics.g3d.environment.PointLight light;

    public PointLight(Color color, Vector3 rel_pos, float intensity) {
        this.color = color;
        this.rel_pos = rel_pos;
        this.intensity = intensity;
        init();
    }

    @Override
    public void init() {
        light = new com.badlogic.gdx.graphics.g3d.environment.PointLight();
        light.set(color, rel_pos, intensity);
    }

    @Override
    public BaseLight getLight() {
        Vector3 vector3 = Vector3Pool.obtain();
        Matrix4 transform = Matrix4Pool.obtain();
        position.getGlobalTransform(transform);
        transform.translate(this.rel_pos).getTranslation(vector3);
        light.setPosition(vector3);
        Vector3Pool.free(vector3);
        Matrix4Pool.free(transform);
        return light;
    }
}
