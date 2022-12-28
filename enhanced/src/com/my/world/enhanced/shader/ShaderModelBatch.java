package com.my.world.enhanced.shader;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;

public class ShaderModelBatch<T extends Shader> extends ModelBatch {

    public T shader;

    public ShaderModelBatch(T shader) {
        super(new ShaderProvider() {
            public Shader getShader(Renderable renderable) { return shader; }
            public void dispose() {}
        });
        this.shader = shader;
    }
}
