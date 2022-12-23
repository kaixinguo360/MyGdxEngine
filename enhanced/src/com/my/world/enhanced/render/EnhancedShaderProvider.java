package com.my.world.enhanced.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

public class EnhancedShaderProvider extends PBRShaderProvider {

    public EnhancedShaderProvider(PBRShaderConfig config) {
        super(config);
    }

    @Override
    public Shader getShader(Renderable renderable) {
        if (renderable.material.has(CustomShaderAttribute.CustomShader)) {
            return renderable.material.get(CustomShaderAttribute.class, CustomShaderAttribute.CustomShader).shader;
        } else if (renderable.material.has(CustomShaderProviderAttribute.CustomShaderProvider)) {
            ShaderProvider shaderProvider = renderable.material.get(CustomShaderProviderAttribute.class, CustomShaderProviderAttribute.CustomShaderProvider).shaderProvider;
            return shaderProvider.getShader(renderable);
        } else {
            return super.getShader(renderable);
        }
    }
}
