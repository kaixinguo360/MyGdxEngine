package com.my.world.module.render.light;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.module.render.Light;
import lombok.NoArgsConstructor;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;

@NoArgsConstructor
public class GLTFDirectionalLight extends Light {

    @Config(fields = { "baseColor", "intensity", "direction" })
    public final DirectionalLightEx light = new DirectionalLightEx();

    public GLTFDirectionalLight(Color baseColor, float intensity, Vector3 direction) {
        light.baseColor.set(baseColor);
        light.intensity = intensity;
        light.direction.set(direction);
    }

    @Override
    public DirectionalLightEx getLight() {
        return light;
    }
}
