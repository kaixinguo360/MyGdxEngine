package com.my.world.enhanced.depthmask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.render.EnhancedFrameBuffer;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.common.Position;
import com.my.world.module.render.Render;
import com.my.world.module.render.RenderSystem;
import com.my.world.module.script.ScriptSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.badlogic.gdx.graphics.GL20.*;

public class DepthMaskScript extends Render implements ScriptSystem.OnStart, CameraSystem.BeforeRender {

    protected static final ModelBatch batch = new ModelBatch();
    protected static final ClearScreenShader clearScreenShader = new ClearScreenShader();

    protected RenderSystem renderSystem;
    protected final EnhancedFrameBuffer fbo;
    protected final int depthMapHandler;

    public final Material material;
    protected final ClearScreenShader.Param param;
    protected final Renderable renderable;

    public final Map<Render, Position> maskEntities = new HashMap<>();
    public final Map<Render, Position> hiddenEntities = new HashMap<>();

    protected final List<Render> visibleMaskEntities = new ArrayList<>();
    protected final List<Render> visibleHiddenEntities = new ArrayList<>();

    public DepthMaskScript() {
        // 创建帧缓冲 (无深度缓存, 有模板缓存)
        int windowWidth = Gdx.graphics.getWidth();
        int windowHeight = Gdx.graphics.getHeight();
        fbo = new EnhancedFrameBuffer(Pixmap.Format.RGBA8888, windowWidth, windowHeight, false, true);

        // 手动创建深度贴图
        depthMapHandler = Gdx.gl.glGenTexture();
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, depthMapHandler);
        Gdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, windowWidth, windowHeight, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        // 手动绑定深度贴图到帧缓冲
        fbo.bind();
        Gdx.gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMapHandler, 0);
        if (Gdx.gl.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Gdx.gl.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE");
        }
        FrameBuffer.unbind();

        // 设置Render
        includeEnv = false;
        shader = clearScreenShader;

        // 创建 ClearScreenShader.Param
        param = new ClearScreenShader.Param();
        param.colorMapHandler = fbo.getColorBufferTexture().getTextureObjectHandle();
        param.depthMapHandler = depthMapHandler;

        // 创建 Material
        material = new Material();

        // 创建 Renderable
        renderable = new Renderable();
        renderable.userData = this.param;
        renderable.material = material;
    }

    @Override
    public void start(Scene scene, Entity entity) {
        this.renderSystem = scene.getSystemManager().getSystem(RenderSystem.class);
    }

    @Override
    public void beforeRender(PerspectiveCamera cam) {
        updateVisibleEntities(cam);
        if (visibleHiddenEntities.size() == 0) return;

        // 切换帧缓冲
        fbo.begin();

        // 渲染DepthMaskObject至帧缓冲
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        batch.begin(cam);
        for (Render render : visibleMaskEntities) {
            batch.render(render, render.shader);
        }
        batch.end();

        // 开启模板测试并清空模板缓冲
        Gdx.gl.glEnable(GL_STENCIL_TEST);
        Gdx.gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        Gdx.gl.glStencilFunc(GL_ALWAYS, 1, 0xFF); //所有片段都要写入模板缓冲
        Gdx.gl.glStencilMask(0xFF); // 设置模板缓冲为可写状态
        Gdx.gl.glClear(GL_STENCIL_BUFFER_BIT);

        // 渲染HiddenObject至帧缓冲
        renderSystem.beginBatch(cam);
        for (Render render : visibleHiddenEntities) {
            renderSystem.addToBatch(render);
        }
        renderSystem.endBatch();

        // 开启被ModelBatch关闭的深度测试
        Gdx.gl.glEnable(GL_DEPTH_TEST);

        // 清除模板之外的部分
        Gdx.gl.glStencilFunc(GL_NOTEQUAL, 1, 0xFF);
        Gdx.gl.glStencilMask(0x00);
        Gdx.gl.glDepthFunc(GL_ALWAYS);
        ShaderUtil.clearScreen(Color.CLEAR, 1);
        Gdx.gl.glDepthFunc(GL_LESS);
        Gdx.gl.glDisable(GL_STENCIL_TEST);

        // 切换帧缓冲
        fbo.end();
    }

    private void updateVisibleEntities(PerspectiveCamera cam) {
        visibleMaskEntities.clear();
        for (Map.Entry<Render, Position> entry : maskEntities.entrySet()) {
            Position position = entry.getValue();
            Render render = entry.getKey();
            render.setTransform(position);
            if (render.isVisible(cam)) {
                visibleMaskEntities.add(render);
            }
        }

        visibleHiddenEntities.clear();
        for (Map.Entry<Render, Position> entry : hiddenEntities.entrySet()) {
            Position position = entry.getValue();
            Render render = entry.getKey();
            render.setTransform(position);
            if (render.isVisible(cam)) {
                visibleHiddenEntities.add(render);
            }
        }
    }

    // ----- Hidden Render Component ----- //

    public void addHiddenRender(Render render, Position position) {
        render.setActive(false);
        hiddenEntities.put(render, position);
    }

    public void removeHiddenRender(Render render) {
        if (!hiddenEntities.containsKey(render)) {
            throw new RuntimeException("No such hidden render component: " + render);
        }
        hiddenEntities.remove(render);
        render.setActive(true);
    }

    public void clearHiddenRender() {
        for (Render render : hiddenEntities.keySet()) {
            render.setActive(true);
        }
        hiddenEntities.clear();
    }

    // ----- Mask Render Component ----- //

    public void addMaskRender(Render render, Position position) {
        render.setActive(false);
        maskEntities.put(render, position);
    }

    public void removeMaskRender(Render render) {
        if (!maskEntities.containsKey(render)) {
            throw new RuntimeException("No such mask render component: " + render);
        }
        maskEntities.remove(render);
        render.setActive(true);
    }

    public void clearMaskRender() {
        for (Render render : maskEntities.keySet()) {
            render.setActive(true);
        }
        maskEntities.clear();
    }

    // ----- Abstract Methods ----- //

    @Override
    public void setTransform(Position position) {

    }

    @Override
    public boolean isVisible(Camera cam) {
        return visibleHiddenEntities.size() != 0;
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        renderables.add(renderable);
//        if (visibleMaskEntities.size() != 0) {
//            renderables.add(renderable);
//        } else {
//            for (Map.Entry<Render, Position> entry : hiddenEntities.entrySet()) {
//                entry.getKey().getRenderables(renderables, pool);
//            }
//        }
    }
}
