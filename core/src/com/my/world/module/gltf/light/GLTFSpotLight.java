package com.my.world.module.gltf.light;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.gltf.GLTFLight;
import lombok.NoArgsConstructor;
import net.mgsx.gltf.scene3d.lights.SpotLightEx;

@NoArgsConstructor
public class GLTFSpotLight extends GLTFLight<SpotLightEx> {

    @Config public Vector3 rel_pos;

    @Config(fields = { "color", "direction", "intensity", "cutoffAngle", "exponent", "range" })
    public final SpotLightEx light = new SpotLightEx();

    public GLTFSpotLight(Color color, Vector3 direction, float intensity, float range) {
        this(color, direction, intensity, range, 0, 0);
    }

    public GLTFSpotLight(Color color, Vector3 direction, float intensity, float range, float cutoffAngle, float exponent) {
        this(color, direction, intensity, range, cutoffAngle, exponent, new Vector3());
    }

    public GLTFSpotLight(Color color, Vector3 direction, float intensity, float range, float cutoffAngle, float exponent, Vector3 rel_pos) {
        this.light.color.set(color);
        this.light.direction.set(direction);
        this.light.intensity = intensity;
        this.light.cutoffAngle = cutoffAngle;
        this.light.exponent = exponent;
        this.light.range = range;
        this.rel_pos = rel_pos;
    }

    @Override
    public SpotLightEx getLight() {
        Matrix4 transform = Matrix4Pool.obtain();
        transform.set(position.getGlobalTransform()).translate(this.rel_pos).getTranslation(light.position);
        Matrix4Pool.free(transform);
        return light;
    }
}
