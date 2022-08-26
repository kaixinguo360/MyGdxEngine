package com.my.world.enhanced.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.math.Vector2;
import net.mgsx.gltf.scene3d.shaders.PBRShader;

public class LightingPassShader extends PBRShader {

    public final static Uniform viewPortSizeUniform = new Uniform("u_viewPortSize");
    public final static Setter viewPortSizeSetter = new LocalSetter() {
        @Override
        public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
            Vector2 viewPortSize = new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            shader.set(inputID, viewPortSize);
        }
    };
    public final int u_viewPortSize;

    public final static Uniform gBuffer0Uniform = new Uniform("u_gBuffer0");
    public final static Uniform gBuffer1Uniform = new Uniform("u_gBuffer1");
    public final static Uniform gBuffer2Uniform = new Uniform("u_gBuffer2");
    public final static Uniform gBuffer3Uniform = new Uniform("u_gBuffer3");
    public final static Setter gBuffer0Setter = GBufferAttribute.setter(GBufferAttribute.GBuffer0);
    public final static Setter gBuffer1Setter = GBufferAttribute.setter(GBufferAttribute.GBuffer1);
    public final static Setter gBuffer2Setter = GBufferAttribute.setter(GBufferAttribute.GBuffer2);
    public final static Setter gBuffer3Setter = GBufferAttribute.setter(GBufferAttribute.GBuffer3);
    public final int u_gBuffer0;
    public final int u_gBuffer1;
    public final int u_gBuffer2;
    public final int u_gBuffer3;

    public LightingPassShader(Renderable renderable, Config config, String prefix) {
        super(renderable, config, prefix);

        // u_viewPortSize
        u_viewPortSize = register(viewPortSizeUniform, viewPortSizeSetter);

        // u_viewPortSize
        u_gBuffer0 = register(gBuffer0Uniform, gBuffer0Setter);
        u_gBuffer1 = register(gBuffer1Uniform, gBuffer1Setter);
        u_gBuffer2 = register(gBuffer2Uniform, gBuffer2Setter);
        u_gBuffer3 = register(gBuffer3Uniform, gBuffer3Setter);
    }
}
