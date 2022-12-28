package com.my.world.enhanced.shader;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.my.world.core.Config;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import static com.badlogic.gdx.graphics.GL20.GL_BACK;
import static com.badlogic.gdx.graphics.GL20.GL_LEQUAL;

@NoArgsConstructor
@AllArgsConstructor
public class SimpleUnlitSingleColorShader extends UnlitSingleColorShader {

    @Config
    public Color color = new Color(1, 1, 1, 1);

    @Config
    public int cullFace = GL_BACK;

    @Config
    public int depthTest = GL_LEQUAL;

    @Config
    public boolean depthMask = true;

    @Override
    public void begin(Camera camera, RenderContext context) {
        context.setBlending(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        context.setCullFace(cullFace);
        context.setDepthTest(depthTest);
        context.setDepthMask(depthMask);
        super.begin(camera, context);
    }

    @Override
    protected Color getColor(Renderable renderable) {
        return color;
    }

}
