package com.my.world.enhanced.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.my.world.core.Config;
import com.my.world.core.Configurable;

import static com.badlogic.gdx.graphics.GL20.*;

public class MrtTestShader implements Shader, Configurable.OnInit {

    @Config private String vertexShaderPath;
    @Config private String fragmentShaderPath;

    private ShaderProgram shaderProgram;

    public MrtTestShader() {
        init();
    }

    public MrtTestShader(String vertexShaderPath, String fragmentShaderPath) {
        this.vertexShaderPath = vertexShaderPath;
        this.fragmentShaderPath = fragmentShaderPath;
        init();
    }

    @Override
    public void init() {
        String vertexShader = this.vertexShaderPath != null
                ? Gdx.files.internal(vertexShaderPath).readString()
                : Gdx.files.classpath("com/my/world/enhanced/render/mrt-test.vs.glsl").readString();
        String fragmentShader = this.fragmentShaderPath != null
                ? Gdx.files.internal(fragmentShaderPath).readString()
                : Gdx.files.classpath("com/my/world/enhanced/render/mrt-test.fs.glsl").readString();
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

    @Override
    public void begin(Camera camera, RenderContext context) {
        context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        context.setDepthTest(GL_LESS);
        context.setDepthMask(true);
        shaderProgram.bind();
        shaderProgram.setUniformMatrix("u_projViewTrans", camera.combined);
    }

    @Override
    public void render(Renderable renderable) {
        shaderProgram.setUniformMatrix("u_worldTrans", renderable.worldTransform);

        TextureAttribute diffuseTexture = renderable.environment.get(TextureAttribute.class, TextureAttribute.Diffuse);
        if (diffuseTexture != null) {
            Gdx.gl.glActiveTexture(GL_TEXTURE0);
            diffuseTexture.textureDescription.texture.bind();
        }

        TextureAttribute normalTexture = renderable.environment.get(TextureAttribute.class, TextureAttribute.Normal);
        if (normalTexture != null) {
            Gdx.gl.glActiveTexture(GL_TEXTURE1);
            normalTexture.textureDescription.texture.bind();
        }

        renderable.meshPart.render(shaderProgram);
    }

    @Override
    public void end() {
    }

}
