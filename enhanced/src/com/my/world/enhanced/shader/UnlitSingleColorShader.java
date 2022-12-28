package com.my.world.enhanced.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.my.world.core.Config;
import com.my.world.core.Configurable;

public abstract class UnlitSingleColorShader implements Shader, Configurable.OnInit {

    @Config protected String vertexShaderPath;
    @Config protected String fragmentShaderPath;

    private ShaderProgram shaderProgram;

    public UnlitSingleColorShader() {
        this(null, null);
    }

    public UnlitSingleColorShader(String vertexShaderPath, String fragmentShaderPath) {
        this.vertexShaderPath = vertexShaderPath;
        this.fragmentShaderPath = fragmentShaderPath;
        init();
    }

    @Override
    public void init() {
        String vertexShader = Gdx.files.classpath(this.vertexShaderPath != null
                ? vertexShaderPath : "com/my/world/enhanced/shader/unlit-single-color.vs.glsl").readString();
        String fragmentShader = Gdx.files.classpath(this.fragmentShaderPath != null
                ? fragmentShaderPath : "com/my/world/enhanced/shader/unlit-single-color.fs.glsl").readString();
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
        shaderProgram.bind();
        shaderProgram.setUniformMatrix("u_projViewTrans", camera.combined);
    }

    @Override
    public void render(Renderable renderable) {
        shaderProgram.setUniformf("u_color", getColor(renderable));
        shaderProgram.setUniformMatrix("u_worldTrans", renderable.worldTransform);
        renderable.meshPart.render(shaderProgram);
    }

    protected abstract Color getColor(Renderable renderable);

    @Override
    public void end() {
    }

}
