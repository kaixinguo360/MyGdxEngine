package com.my.demo.builder.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.common.Position;
import com.my.world.module.render.Render;
import com.my.world.module.render.RenderSystem;
import com.my.world.module.script.ScriptSystem;

import java.util.HashMap;
import java.util.Map;

import static com.badlogic.gdx.graphics.GL20.*;

public class DepthMaskScript implements ScriptSystem.OnStart, CameraSystem.AfterRender {

    protected Entity selfEntity;
    protected Position selfPosition;
    protected Render selfRender;

    protected RenderSystem renderSystem;

    protected final Map<Render, Position> hiddenEntities = new HashMap<>();

    protected static final ModelBatch batch;
    protected static final ShaderProgram depthShader;
    protected static final ShaderProgram clearShader;

    protected static final int depthMapHandler;
    protected static final FrameBuffer fbo;
    protected static Mesh mesh;

    static {
        // 初始化ModelBatch
        batch = new ModelBatch();

        // 初始化depthShader渲染器
        depthShader = new ShaderProgram(
                Gdx.files.classpath("com/my/demo/builder/test/enhanced-depth-mask.vertex.glsl").readString(),
                Gdx.files.classpath("com/my/demo/builder/test/enhanced-depth-mask.fragment.glsl").readString()
        );
        if (!depthShader.isCompiled()) {
            throw new GdxRuntimeException(depthShader.getLog());
        }

        // 初始化clearShader渲染器
        clearShader = new ShaderProgram(
                Gdx.files.classpath("com/my/demo/builder/test/clear.vertex.glsl").readString(),
                Gdx.files.classpath("com/my/demo/builder/test/clear.fragment.glsl").readString()
        );
        if (!clearShader.isCompiled()) {
            throw new GdxRuntimeException(clearShader.getLog());
        }

        // 初始化覆盖屏幕的矩形网格
        VertexAttributes attributes = new VertexAttributes(
                VertexAttribute.Position(),
                VertexAttribute.TexCoords(0)
        );
        float[] vertices = {
                1f, 1f, 0.0f,    1f, 1f,// 右上角
                1f, -1f, 0.0f,   1f, 0f,// 右下角
                -1f, -1f, 0.0f,  0f, 0f,// 左下角
                -1f, 1f, 0.0f,   0f, 1f,// 左上角
        };
        short[] indices = {
                0, 1, 3, // 第一个三角形
                1, 2, 3  // 第二个三角形
        };
        mesh = new Mesh(true, 4, 6, attributes);
        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        // 创建帧缓冲 (无深度缓存, 有模板缓存)
        int windowWidth = Gdx.graphics.getWidth();
        int windowHeight = Gdx.graphics.getHeight();
        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, windowWidth, windowHeight, false, true);

        // 手动创建深度贴图
        depthMapHandler = Gdx.gl.glGenTexture();
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, depthMapHandler);
        Gdx.gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, windowWidth, windowHeight, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        Gdx.gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        // 手动绑定深度贴图到帧缓冲
        Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, fbo.getFramebufferHandle());
        Gdx.gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthMapHandler, 0);
        Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    @Override
    public void start(Scene scene, Entity entity) {
        this.selfEntity = entity;
        this.selfPosition = entity.getComponent(Position.class);
        this.selfRender = entity.getComponent(Render.class);
        this.selfRender.setActive(false);

        this.renderSystem = scene.getSystemManager().getSystem(RenderSystem.class);
    }

    @Override
    public void afterRender(PerspectiveCamera cam) {
        selfRender.setTransform(selfPosition);
        selfRender.isVisible(cam);

        // 切换帧缓冲
        fbo.begin();

        // 渲染DepthMaskObject至帧缓冲
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        if (selfRender.isVisible(cam)) {
            batch.begin(cam);
            batch.render(selfRender, selfRender.shader);
            batch.end();
        }

        // 开启模板测试并清空模板缓冲
        Gdx.gl.glEnable(GL_STENCIL_TEST);
        Gdx.gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        Gdx.gl.glStencilFunc(GL_ALWAYS, 1, 0xFF); //所有片段都要写入模板缓冲
        Gdx.gl.glStencilMask(0xFF); // 设置模板缓冲为可写状态
        Gdx.gl.glClear(GL_STENCIL_BUFFER_BIT);

        // 渲染HiddenObject至帧缓冲
        renderSystem.beginBatch(cam);
        for (Map.Entry<Render, Position> entry : hiddenEntities.entrySet()) {
            Position position = entry.getValue();
            Render render = entry.getKey();
            render.setTransform(position);
            if (render.isVisible(cam)) {
                renderSystem.addToBatch(render);
            }
        }
        renderSystem.endBatch();

        // 开启被ModelBatch关闭的深度测试
        Gdx.gl.glEnable(GL_DEPTH_TEST);

        // 清除模板之外的部分
        clearShader.bind();
        clearShader.setUniformf("u_color", Color.PINK);
        clearShader.setUniformf("u_depth", 1);
        Gdx.gl.glStencilFunc(GL_NOTEQUAL, 1, 0xFF);
        Gdx.gl.glStencilMask(0x00);
        Gdx.gl.glDepthFunc(GL_ALWAYS);
        mesh.render(clearShader, GL_TRIANGLES);
        Gdx.gl.glDepthFunc(GL_LESS);
        Gdx.gl.glDisable(GL_STENCIL_TEST);

        // 切换帧缓冲
        fbo.end();

        // 渲染帧缓冲至屏幕
        depthShader.bind();

        Gdx.gl.glActiveTexture(GL_TEXTURE0); //激活纹理单元, 接下来的glBindTexture函数调用会绑定这个纹理到当前激活的纹理单元
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, fbo.getColorBufferTexture().getTextureObjectHandle());
        depthShader.setUniformi("u_colorMap", 0);

        Gdx.gl.glActiveTexture(GL_TEXTURE1);
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, depthMapHandler);
        depthShader.setUniformi("u_depthMap", 1);

        mesh.render(depthShader, GL_TRIANGLES);
    }

    public void addRender(Render render, Position position) {
        if (render.isActive()) {
            render.setActive(false);
            hiddenEntities.put(render, position);
        }
    }

    public void removeRender(Render render) {
        if (!hiddenEntities.containsKey(render)) {
            throw new RuntimeException("No such render: " + render);
        }
        hiddenEntities.remove(render);
        render.setActive(true);
    }

    public void clearRender() {
        for (Render render : hiddenEntities.keySet()) {
            render.setActive(true);
        }
        hiddenEntities.clear();
    }
}
