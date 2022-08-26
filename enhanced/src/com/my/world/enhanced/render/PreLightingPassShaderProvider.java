package com.my.world.enhanced.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import net.mgsx.gltf.scene3d.shaders.PBRShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

class PreLightingPassShaderProvider extends PBRShaderProvider {

    public static PBRShaderConfig createPreLightingPassShaderConfig() {
        PBRShaderConfig config = new PBRShaderConfig();
        config.numPointLights = 5;
        config.numDirectionalLights = 3;
        config.numSpotLights = 1;
        config.numBones = 0;
        config.vertexShader = Gdx.files.classpath("com/my/world/enhanced/render/pre-lighting-pass.vs.glsl").readString();
        config.fragmentShader = Gdx.files.classpath("com/my/world/enhanced/render/pre-lighting-pass.fs.glsl").readString();
        return config;
    }

    public PreLightingPassShaderProvider(PBRShaderConfig config) {
        super(config);
    }

    @Override
    public String createPrefixBase(Renderable renderable, PBRShaderConfig config) {

        String defaultPrefix = DefaultShader.createPrefix(renderable, config);
        String prefix = "#version 330 core\n" + "#define GLSL3\n";
        if(config.prefix != null) prefix += config.prefix;
        prefix += defaultPrefix;

        return prefix;
    }

    protected PBRShader createShader(Renderable renderable, PBRShaderConfig config, String prefix){
        return new LightingPassShader(renderable, config, prefix);
    }
}
