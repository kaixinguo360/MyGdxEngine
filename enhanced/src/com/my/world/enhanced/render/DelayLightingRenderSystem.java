package com.my.world.enhanced.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.FlushablePool;
import com.my.world.module.render.Render;

import java.nio.IntBuffer;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.graphics.GL30.*;

public class DelayLightingRenderSystem extends EnhancedRenderSystem {

    protected final EnhancedFrameBuffer fbo;
    protected final IntBuffer attachments1;
    protected final IntBuffer attachments2;

    public DelayLightingRenderSystem() {
        int windowWidth = Gdx.graphics.getWidth();
        int windowHeight = Gdx.graphics.getHeight();
        fbo = new EnhancedFrameBuffer(Pixmap.Format.RGBA8888, windowWidth, windowHeight, true, true);
        fbo.bind();

        // 手动创建颜色附件1
        int gPosition = Gdx.gl.glGenTexture();
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, gPosition);
        Gdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight, 0, GL_RGB, GL_FLOAT, null);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        Gdx.gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, gPosition, 0);

        // 手动创建颜色附件2
        int gNormal = Gdx.gl.glGenTexture();
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, gNormal);
        Gdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, windowWidth, windowHeight, 0, GL_RGB, GL_FLOAT, null);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        Gdx.gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, gNormal, 0);

        // 手动创建颜色附件3
        int gAlbedoSpec = Gdx.gl.glGenTexture();
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, gAlbedoSpec);
        Gdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, windowWidth, windowHeight, 0, GL_RGBA, GL_FLOAT, null);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        Gdx.gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT2, GL_TEXTURE_2D, gAlbedoSpec, 0);

        attachments1 = BufferUtils.newIntBuffer(3);
        attachments1.put(new int[] {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2});

        attachments2 = BufferUtils.newIntBuffer(1);
        attachments2.put(new int[] {GL_COLOR_ATTACHMENT0});

        // 手动绑定颜色附件到帧缓冲
        if (Gdx.gl.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Gdx.gl.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE");
        }
        FrameBuffer.unbind();
    }

    protected final FlushablePool<Renderable> renderablesPool = new FlushablePool<Renderable>() {

        @Override
        protected Renderable newObject() {
            return new Renderable();
        }

        @Override
        public Renderable obtain() {
            Renderable renderable = super.obtain();
            renderable.environment = null;
            renderable.material = null;
            renderable.meshPart.set("", null, 0, 0, 0);
            renderable.shader = null;
            renderable.userData = null;
            return renderable;
        }
    };

    protected final Array<Renderable> renderables = new Array<>();

    // ----- RenderBatch ----- //

    @Override
    public void beginBatch(Camera cam) {
        if (currentCamera != null) throw new RuntimeException("Current camera is not null");
        currentCamera = cam;
    }

    @Override
    public void endBatch() {
        if (currentCamera == null) throw new RuntimeException("Current camera is null");
        lightingRender();
        forwardRender();
        renderablesPool.flush();
        renderables.clear();
        currentCamera = null;
    }

    @Override
    public void addToBatch(Render render) {
        if (currentCamera == null) throw new RuntimeException("Current camera is null");
        if (!render.isVisible(currentCamera)) {
            return;
        }
        final int offset = renderables.size;
        render.getRenderables(renderables, renderablesPool);
        for (int i = offset; i < renderables.size; i++) {
            Renderable renderable = renderables.get(i);
            if (currentEnvironment != null && render.includeEnv) {
                renderable.environment = currentEnvironment;
            }
            if (render.shader != null) {
                renderable.shader = render.shader;
            }
        }
    }

    // ----- Render ----- //

    protected ShaderProvider lightingShaderProvider = new ShaderProvider() {
        public final Shader lightingShader = new DefaultPreLightingPassShader();
        public Shader getShader(Renderable renderable) { return lightingShader; }
        public void dispose() {}
    };

    protected void lightingRender() {
        fbo.begin();

        Gdx.gl30.glDrawBuffers(3, attachments1);
        render(renderables, batch.getRenderContext(), batch.getRenderableSorter(), lightingShaderProvider, currentCamera);
        Gdx.gl30.glDrawBuffers(1, attachments2);

        fbo.end();
    }

    protected void forwardRender() {
        render(renderables, batch.getRenderContext(), batch.getRenderableSorter(), batch.getShaderProvider(), currentCamera);
    }

    public static void render(Array<Renderable> renderables, RenderContext context, RenderableSorter sorter, ShaderProvider shaderProvider, Camera camera) {
        if (context != null) context.begin();
        if (sorter != null) sorter.sort(camera, renderables);
        Shader currentShader = null;
        for (int i = 0; i < renderables.size; i++) {
            final Renderable renderable = renderables.get(i);
            Shader shader = shaderProvider.getShader(renderable);
            if (currentShader != shader) {
                if (currentShader != null) currentShader.end();
                currentShader = shader;
                currentShader.begin(camera, context);
            }
            currentShader.render(renderable);
        }
        if (currentShader != null) currentShader.end();
        if (context != null) context.end();
    }
}
