package com.my.world.module.render.enhanced;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.my.world.core.Config;
import com.my.world.core.Loadable;

public class EnhancedShader implements Shader, Loadable.OnInit {

    @Config private String vertexShaderPath;
    @Config private String fragmentShaderPath;

    private ShaderProgram shaderProgram;

    public EnhancedShader() {
        init();
    }

    public EnhancedShader(String vertexShaderPath, String fragmentShaderPath) {
        this.vertexShaderPath = vertexShaderPath;
        this.fragmentShaderPath = fragmentShaderPath;
        init();
    }

    @Override
    public void init() {
        String vertexShader = this.vertexShaderPath != null
                ? Gdx.files.internal(vertexShaderPath).readString()
                : Gdx.files.classpath("com/my/world/module/render/enhanced/enhanced.vertex.glsl").readString();
        String fragmentShader = this.fragmentShaderPath != null
                ? Gdx.files.internal(fragmentShaderPath).readString()
                : Gdx.files.classpath("com/my/world/module/render/enhanced/enhanced.fragment.glsl").readString();
        shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
        if (!shaderProgram.isCompiled()) {
            throw new GdxRuntimeException(shaderProgram.getLog());
        }
    }

    @Override
    public void dispose() {
        shaderProgram.dispose();
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return true;
    }

    private Environment currentEnvironment;
    private Camera currentCamera;
    private RenderContext currentContext;

    @Override
    public void begin(Camera camera, RenderContext context) {
        currentEnvironment = null;
        clearEnvironment();

        currentCamera = camera;

        currentContext = context;
        currentContext.setDepthTest(GL20.GL_LEQUAL);
        currentContext.setCullFace(GL20.GL_BACK);

        shaderProgram.bind();
        shaderProgram.setUniformMatrix("u_projViewTrans", camera.combined);
        shaderProgram.setUniformf("u_viewPosition", camera.position);
    }

    @Override
    public void end() {
    }

    @Override
    public void render(Renderable renderable) {
        shaderProgram.setUniformMatrix("u_worldTrans", renderable.worldTransform);

        setEnvironment(renderable.environment);
        setAmbientColor(renderable.material);
        setDiffuseColor(renderable.material);
        setSpecularColor(renderable.material);

        renderable.meshPart.render(shaderProgram);
    }

    // ----- Set Environment ----- //

    private void setEnvironment(Environment environment) {
        if (environment == null) {
            currentEnvironment = null;
            clearEnvironment();
        } else {
            if (currentEnvironment != environment) {
                currentEnvironment = environment;
                setGlobalAmbientLight(environment);
                setPointLights(environment);
            }
        }
    }

    private void clearEnvironment() {
        shaderProgram.setUniformf("u_globalAmbientLight", Color.WHITE);
        shaderProgram.setUniformi("u_numPointLights", 0);
    }

    // ----- Set Global Ambient Light ----- //

    private void setGlobalAmbientLight(Environment environment) {
        if (environment.has(ColorAttribute.AmbientLight)) {
            // Enable
            ColorAttribute colorAttribute = (ColorAttribute) environment.get(ColorAttribute.AmbientLight);
            shaderProgram.setUniformf("u_globalAmbientLight", colorAttribute.color);
        } else {
            // Disable
            shaderProgram.setUniformf("u_globalAmbientLight", Color.WHITE);
        }
    }

    // ----- Set Point Lights ----- //

    private final static int MaxPointLights = 30;
    private final static Vector3 tmpV = new Vector3();
    private void setPointLights(Environment environment) {
        if (environment.has(EnhancedPointLightsAttribute.Type)) {
            // Enable
            EnhancedPointLightsAttribute lightsAttribute = (EnhancedPointLightsAttribute) environment.get(EnhancedPointLightsAttribute.Type);
            int numPointLights = Math.min(lightsAttribute.lights.size(), MaxPointLights);
            for (int i = 0; i < numPointLights; i++) {
                EnhancedPointLight light = lightsAttribute.lights.get(i);
                shaderProgram.setUniformf("u_pointLights[" + i +  "].position", light.position.getGlobalTransform().getTranslation(tmpV));
                shaderProgram.setUniformf("u_pointLights[" + i +  "].constant", light.constant);
                shaderProgram.setUniformf("u_pointLights[" + i +  "].linear", light.linear);
                shaderProgram.setUniformf("u_pointLights[" + i +  "].quadratic", light.quadratic);
                shaderProgram.setUniformf("u_pointLights[" + i +  "].material.ambient", light.ambient != null ? light.ambient : Color.BLACK);
                shaderProgram.setUniformf("u_pointLights[" + i +  "].material.diffuse", light.diffuse != null ? light.diffuse : Color.BLACK);
                shaderProgram.setUniformf("u_pointLights[" + i +  "].material.specular", light.specular != null ? light.specular : Color.WHITE);
            }
            shaderProgram.setUniformi("u_numPointLights", numPointLights);
        } else {
            // Disable
            shaderProgram.setUniformi("u_numPointLights", 0);
        }
    }

    // ----- Set Object Material Color ----- //

    private static class DataSourceType {
        private final static int Texture = 2;
        private final static int Material = 1;
        private final static int Vertex = 0;
        private final static int NotSet = -1;
    }
    private void setAmbientColor(Material material) {
        if (material.has(TextureAttribute.Ambient)) {
            // Use Texture
            TextureAttribute textureAttribute = (TextureAttribute) material.get(TextureAttribute.Ambient);
            final int unit = currentContext.textureBinder.bind(textureAttribute.textureDescription);
            shaderProgram.setUniformi("u_ambientTexture", unit);
            shaderProgram.setUniformi("u_ambientType", DataSourceType.Texture);
        } else if (material.has(ColorAttribute.Ambient)) {
            // Use Material Color
            ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Ambient);
            shaderProgram.setUniformf("u_materialAmbientColor", colorAttribute.color);
            shaderProgram.setUniformi("u_ambientType", DataSourceType.Material);
        } else {
            // Not Set
            shaderProgram.setUniformi("u_ambientType", DataSourceType.NotSet);
        }
    }
    private void setDiffuseColor(Material material) {
        if (material.has(TextureAttribute.Diffuse)) {
            // Use Texture
            TextureAttribute textureAttribute = (TextureAttribute) material.get(TextureAttribute.Diffuse);
            final int unit = currentContext.textureBinder.bind(textureAttribute.textureDescription);
            shaderProgram.setUniformi("u_diffuseTexture", unit);
            shaderProgram.setUniformi("u_diffuseType", DataSourceType.Texture);
        } else if (material.has(ColorAttribute.Diffuse)) {
            // Use Material Color
            ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Diffuse);
            shaderProgram.setUniformf("u_materialDiffuseColor", colorAttribute.color);
            shaderProgram.setUniformi("u_diffuseType", DataSourceType.Material);
        } else {
            // Use Vertex Color
            shaderProgram.setUniformi("u_diffuseType", DataSourceType.Vertex);
        }
    }
    private void setSpecularColor(Material material) {
        if (material.has(TextureAttribute.Specular)) {
            // Use Texture
            TextureAttribute textureAttribute = (TextureAttribute) material.get(TextureAttribute.Specular);
            final int unit = currentContext.textureBinder.bind(textureAttribute.textureDescription);
            shaderProgram.setUniformi("u_specularTexture", unit);
            shaderProgram.setUniformi("u_specularType", DataSourceType.Texture);
        } else if (material.has(ColorAttribute.Specular)) {
            // Use Material Color
            ColorAttribute colorAttribute = (ColorAttribute) material.get(ColorAttribute.Specular);
            shaderProgram.setUniformf("u_materialSpecularColor", colorAttribute.color);
            shaderProgram.setUniformi("u_specularType", DataSourceType.Material);
        } else {
            // Not Set
            shaderProgram.setUniformi("u_specularType", DataSourceType.NotSet);
        }
    }
}
