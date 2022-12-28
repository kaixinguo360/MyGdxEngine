package com.my.world.enhanced.shader.depthmask;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.my.world.enhanced.render.CustomShaderAttribute;
import com.my.world.enhanced.shader.UnlitSingleColorShader;

import static com.badlogic.gdx.graphics.GL20.GL_LEQUAL;
import static com.badlogic.gdx.graphics.GL20.GL_NONE;

public class DepthMaskShader extends UnlitSingleColorShader {

    @Override
    public void begin(Camera camera, RenderContext context) {
        context.setBlending(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        context.setCullFace(GL_NONE);
        context.setDepthTest(GL_LEQUAL);
        context.setDepthMask(true);
        super.begin(camera, context);
    }

    @Override
    protected Color getColor(Renderable renderable) {
        DepthMaskAttribute attribute = renderable.material.get(DepthMaskAttribute.class, CustomShaderAttribute.CustomShader);
        return attribute.color;
    }

}
