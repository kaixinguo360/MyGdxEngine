package com.my.world.enhanced.portal;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;

import static com.badlogic.gdx.graphics.GL20.GL_LESS;

public class ClearScreenShader implements Shader {

    @Override
    public void init() {

    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable instance) {
        return instance.userData instanceof Param;
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        context.setDepthTest(GL_LESS);
        context.setDepthMask(true);
    }

    @Override
    public void render(Renderable renderable) {
        Param p = (Param) renderable.userData;
        ShaderUtil.clearScreen(p.color, p.colorMapHandler, p.depth, p.depthMapHandler);
    }

    @Override
    public void end() {

    }

    @Override
    public void dispose() {

    }

    public static class Param {
        public Color color;
        public Integer colorMapHandler;
        public Float depth;
        public Integer depthMapHandler;
    }
}
