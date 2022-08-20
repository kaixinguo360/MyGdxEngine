package com.my.world.enhanced.render;

import com.my.world.core.Configurable;
import com.my.world.core.Context;
import com.my.world.module.render.RenderSystem;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

import java.util.HashMap;
import java.util.Map;

public class EnhancedRenderSystem extends RenderSystem implements Configurable.OnLoad, Configurable.OnDump {

    public EnhancedRenderSystem() {
        this(5, 3, 1, 0);
    }

    public EnhancedRenderSystem(int numPointLights, int numDirectionalLights, int numSpotLights, int numBones) {
        PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
        config.numPointLights = numPointLights;
        config.numDirectionalLights = numDirectionalLights;
        config.numSpotLights = numSpotLights;
        config.numBones = numBones;
        init(config);
    }

    public EnhancedRenderSystem(PBRShaderConfig config) {
        init(config);
    }

    protected void init(PBRShaderConfig config) {
        shaderProvider = new EnhancedShaderProvider(config);
        renderableSorter = new EnhancedRenderableSorter();
    }

    @Override
    public void load(Map<String, Object> config, Context context) {

    }

    @Override
    public Map<String, Object> dump(Context context) {
        return new HashMap<>();
    }
}
