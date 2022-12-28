package com.my.world.enhanced.script;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.my.world.core.Config;
import com.my.world.enhanced.shader.ShaderModelBatch;
import com.my.world.enhanced.shader.SimpleUnlitSingleColorShader;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.render.Render;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.graphics.GL20.GL_LEQUAL;
import static com.badlogic.gdx.graphics.GL20.GL_NONE;

public class HighlightScript implements CameraSystem.AfterRender {

    @Config
    public Color color = new Color(1, 1, 0, 0.5f);

    @Config
    public boolean enableDepthTest = true;

    public final List<Render> renders = new ArrayList<>();
    protected final ShaderModelBatch<SimpleUnlitSingleColorShader> batch = new ShaderModelBatch<>(new SimpleUnlitSingleColorShader());

    @Override
    public void afterRender(Camera cam) {
        batch.shader.color.set(color);
        batch.shader.depthTest = enableDepthTest ? GL_LEQUAL : GL_NONE;
        batch.shader.depthMask = false;
        batch.begin(cam);
        for (Render render : renders) {
            batch.render(render);
        }
        batch.end();
    }
}
