package com.my.world.enhanced.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.shaders.PBRShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

class LightingPassShaderProvider extends PBRShaderProvider {

    public static PBRShaderConfig createLightingPassShaderConfig() {
        PBRShaderConfig config = new PBRShaderConfig();
        config.numPointLights = 5;
        config.numDirectionalLights = 3;
        config.numSpotLights = 1;
        config.numBones = 0;
        config.vertexShader = Gdx.files.classpath("com/my/world/enhanced/render/lighting-pass.vs.glsl").readString();
        config.fragmentShader = Gdx.files.classpath("com/my/world/enhanced/render/lighting-pass.fs.glsl").readString();
        return config;
    }

    public LightingPassShaderProvider(PBRShaderConfig config) {
        super(config);
    }

    @Override
    public Shader createShader(Renderable renderable) {

        PBRShaderConfig config = (PBRShaderConfig) this.config;
        String prefix = createPrefixBase(renderable, config);

        // IBL options
        PBRCubemapAttribute specualarCubemapAttribute = null;
        if(renderable.environment != null){
            if(renderable.environment.has(PBRCubemapAttribute.SpecularEnv)){
                prefix += "#define diffuseSpecularEnvSeparateFlag\n";
                specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.SpecularEnv);
            }else if(renderable.environment.has(PBRCubemapAttribute.DiffuseEnv)){
                specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.DiffuseEnv);
            }else if(renderable.environment.has(PBRCubemapAttribute.EnvironmentMap)){
                specualarCubemapAttribute = renderable.environment.get(PBRCubemapAttribute.class, PBRCubemapAttribute.EnvironmentMap);
            }
            if(specualarCubemapAttribute != null){
                prefix += "#define USE_IBL\n";

                boolean textureLodSupported;
                if(isGL3()){
                    textureLodSupported = true;
                }else if(Gdx.graphics.supportsExtension("EXT_shader_texture_lod")){
                    prefix += "#define USE_TEXTURE_LOD_EXT\n";
                    textureLodSupported = true;
                }else{
                    textureLodSupported = false;
                }

                Texture.TextureFilter textureFilter = specualarCubemapAttribute.textureDescription.minFilter != null ? specualarCubemapAttribute.textureDescription.minFilter : specualarCubemapAttribute.textureDescription.texture.getMinFilter();
                if(textureLodSupported && textureFilter.equals(Texture.TextureFilter.MipMap)){
                    prefix += "#define USE_TEX_LOD\n";
                }

                if(renderable.environment.has(PBRTextureAttribute.BRDFLUTTexture)){
                    prefix += "#define brdfLUTTexture\n";
                }
            }
            // TODO check GLSL extension 'OES_standard_derivatives' for WebGL
            // TODO check GLSL extension 'EXT_SRGB' for WebGL

            if(renderable.environment.has(ColorAttribute.AmbientLight)){
                prefix += "#define ambientLightFlag\n";
            }
        }

        // SRGB
        prefix += createPrefixSRGB(renderable, config);

        // Fog
        if(renderable.environment.has(FogAttribute.FogEquation)){
            prefix += "#define fogEquationFlag\n";
        }

        PBRShader shader = createShader(renderable, config, prefix);
        checkShaderCompilation(shader.program);

        // prevent infinite loop (TODO remove this for libgdx 1.9.12+)
        if(!shader.canRender(renderable)){
            throw new GdxRuntimeException("cannot render with this shader");
        }

        return shader;
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
