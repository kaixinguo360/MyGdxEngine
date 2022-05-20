package com.my.demo.builder.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.my.demo.attribute.CustomShaderAttribute;
import com.my.world.core.Config;
import com.my.world.core.Configurable;

public class DepthMaskShader implements Shader, Configurable.OnInit {

    @Config private String vertexShaderPath;
    @Config private String fragmentShaderPath;

    private ShaderProgram shaderProgram;

    public DepthMaskShader() {
        init();
    }

    public DepthMaskShader(String vertexShaderPath, String fragmentShaderPath) {
        this.vertexShaderPath = vertexShaderPath;
        this.fragmentShaderPath = fragmentShaderPath;
        init();
    }

    @Override
    public void init() {
        String vertexShader = this.vertexShaderPath != null
                ? Gdx.files.internal(vertexShaderPath).readString()
                : Gdx.files.classpath("com/my/demo/builder/test/depth-mask.vertex.glsl").readString();
        String fragmentShader = this.fragmentShaderPath != null
                ? Gdx.files.internal(fragmentShaderPath).readString()
                : Gdx.files.classpath("com/my/demo/builder/test/depth-mask.fragment.glsl").readString();
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
        context.setBlending(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        context.setDepthMask(true);
        shaderProgram.bind();
        shaderProgram.setUniformMatrix("u_projViewTrans", camera.combined);
    }

    @Override
    public void render(Renderable renderable) {
        DepthMaskAttribute attribute = renderable.material.get(DepthMaskAttribute.class, CustomShaderAttribute.CustomShader);
        shaderProgram.setUniformf("u_color", attribute.color);
        shaderProgram.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        renderable.meshPart.render(shaderProgram);
    }

    @Override
    public void end() {
    }

}
