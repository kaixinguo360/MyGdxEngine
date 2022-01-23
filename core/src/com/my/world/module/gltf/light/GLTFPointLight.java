package com.my.world.module.gltf.light;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.gltf.GLTFLight;
import lombok.NoArgsConstructor;
import net.mgsx.gltf.scene3d.lights.PointLightEx;

@NoArgsConstructor
public class GLTFPointLight extends GLTFLight<PointLightEx> {

    @Config public Vector3 rel_pos;

    @Config(fields = { "color", "intensity", "range" })
    public final PointLightEx light = new PointLightEx();

    public GLTFPointLight(Color color, float intensity, float range) {
        this(color, intensity, range, new Vector3());
    }

    public GLTFPointLight(Color color, float intensity, float range, Vector3 rel_pos) {
        this.light.color.set(color);
        this.light.intensity = intensity;
        this.light.range = range;
        this.rel_pos = rel_pos;
    }

    @Override
    public PointLightEx getLight() {
        Matrix4 transform = Matrix4Pool.obtain();
        transform.set(position.getGlobalTransform()).translate(this.rel_pos).getTranslation(light.position);
        Matrix4Pool.free(transform);
        return light;
    }
}
