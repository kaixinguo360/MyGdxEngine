package com.my.world.module.gltf.light;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.render.Light;
import lombok.NoArgsConstructor;
import net.mgsx.gltf.scene3d.lights.SpotLightEx;

@NoArgsConstructor
public class GLTFSpotLight extends Light {

    @Config public Vector3 rel_pos;
    @Config public Vector3 direction;

    @Config public float outerConeAngleDeg;
    @Config public float innerConeAngleDeg;

    @Config(fields = { "color", "intensity", "range" })
    public final SpotLightEx light = new SpotLightEx();

    public GLTFSpotLight(Color color, Vector3 direction, float intensity, float range) {
        this(color, direction, intensity, range, 30, 20);
    }

    public GLTFSpotLight(Color color, Vector3 direction, float intensity, float range, float outerConeAngleDeg, float innerConeAngleDeg) {
        this(color, direction, intensity, range, outerConeAngleDeg, innerConeAngleDeg, new Vector3());
    }

    public GLTFSpotLight(Color color, Vector3 direction, float intensity, float range, float outerConeAngleDeg, float innerConeAngleDeg, Vector3 rel_pos) {
        this.light.color.set(color);
        this.light.intensity = intensity;
        this.light.range = range;
        this.outerConeAngleDeg = outerConeAngleDeg;
        this.innerConeAngleDeg = innerConeAngleDeg;
        this.rel_pos = rel_pos;
        this.direction = direction;
    }

    @Override
    public SpotLightEx getLight() {
        Matrix4 transform = Matrix4Pool.obtain();

        position.getGlobalTransform(transform).translate(this.rel_pos);
        transform.getTranslation(light.position);
        light.direction.set(direction).rot(transform);
        light.setConeDeg(outerConeAngleDeg, innerConeAngleDeg);

        Matrix4Pool.free(transform);
        return light;
    }
}
