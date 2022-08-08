package com.my.world.enhanced.portal.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.util.Pool;
import com.my.world.enhanced.portal.Portal;
import com.my.world.enhanced.render.EnhancedFrameBuffer;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.common.Position;
import com.my.world.module.render.Render;
import com.my.world.module.render.RenderSystem;
import com.my.world.module.script.ScriptSystem;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.badlogic.gdx.graphics.GL20.*;

/**
 * Implement the basic render function of portal
 */
public class PortalRenderScript implements ScriptSystem.OnStart, CameraSystem.AfterRender {

    @Config public float frustumOffset = 0;
    @Config public Shader shader;

    protected CameraSystem cameraSystem;
    protected RenderSystem renderSystem;

    protected Portal portal;
    protected Render render;

    protected final PerspectiveCamera camera = new PerspectiveCamera();

    protected static int level = 0;
    protected static final ModelBatch batch = new ModelBatch();
    protected static final SpriteBatch spriteBatch = new SpriteBatch();
    protected static final FrameBuffer fbo = new EnhancedFrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

    @Override
    public void start(Scene scene, Entity entity) {
        this.cameraSystem = scene.getSystemManager().getSystem(CameraSystem.class);
        this.renderSystem = scene.getSystemManager().getSystem(RenderSystem.class);

        this.portal = entity.getComponent(Portal.class);
        this.render = entity.getComponent(Render.class);
        this.render.setActive(false);
    }

    @Override
    public void afterRender(PerspectiveCamera cam) {
        if (level <= 0 && cam != camera) {
            level++;

            boolean isVisible = setCamera(cam);
            if (isVisible) {
                renderPortal(cam);
            }

            level--;
        }
    }

    protected boolean setCamera(PerspectiveCamera cam) {
        render.setTransform(portal.selfPosition);

        if (!render.isVisible(cam)) {
            return false;
        }

        Matrix4 targetTransform = portal.getTargetTransform();

        Vector3 tmpV = Vector3Pool.obtain();
        Matrix4 tmpM = Matrix4Pool.obtain();

        portal.getTransferTransform(tmpM, targetTransform);

        this.camera.far = cam.far;
        this.camera.near = cam.near;
        this.camera.fieldOfView = cam.fieldOfView;
        this.camera.viewportWidth = cam.viewportWidth;
        this.camera.viewportHeight = cam.viewportHeight;
        this.camera.position.set(cam.position).mul(tmpM);
        this.camera.direction.set(cam.direction).rot(tmpM).nor();
        this.camera.up.set(cam.up).rot(tmpM);

        // 基于相机近剪裁平面实现传送门前方遮挡物体剔除 (问题: 不精确)
        this.camera.near = -frustumOffset + targetTransform.getTranslation(tmpV).sub(this.camera.position).dot(this.camera.direction);
        if (this.camera.near <= 0) {
            this.camera.near = 0.01f;
        }

        this.camera.update();

        return true;
    }

    protected void renderPortal(PerspectiveCamera cam) {

        // 传送门外远程物体虚像
        if (showOuterVirtualEntities) {
            renderVirtualEntities(cam, outerVirtualEntities);
        }

        // 准备工作
        Gdx.gl.glEnable(GL_STENCIL_TEST);
        Gdx.gl.glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        Gdx.gl.glEnable(GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL_CULL_FACE);

        // 渲染传送门轮廓至模板
        Gdx.gl.glStencilFunc(GL_ALWAYS, 1, 0xFF); //所有片段都要写入模板缓冲
        Gdx.gl.glStencilMask(0xFF); // 设置模板缓冲为可写状态
        Gdx.gl.glColorMask(false, false, false, false);
        Gdx.gl.glClear(GL_STENCIL_BUFFER_BIT);
        Gdx.gl.glFrontFace(GL_CW);
        batch.begin(cam);
        batch.render(render, shader);
        batch.end();
        Gdx.gl.glFrontFace(GL_CCW);
        Gdx.gl.glColorMask(true, true, true, true);

        // 渲染传送门内场景至帧缓冲
        fbo.begin();
        cameraSystem.callAllBeforeRenderScript(camera);
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        // 传送门内场景
        renderSystem.render(camera);
        // 传送门内本地物体虚像
        if (showInnerVirtualEntities) {
            renderVirtualEntities(camera, innerVirtualEntities);
        }
        cameraSystem.callAllAfterRenderScript(camera);
        fbo.end();

        // 渲染帧缓冲内容至传送门轮廓
        Gdx.gl.glEnable(GL_STENCIL_TEST);
        Gdx.gl.glStencilFunc(GL_EQUAL, 1, 0xFF);
        Gdx.gl.glStencilMask(0x00);
        spriteBatch.begin();
        spriteBatch.draw(
                fbo.getColorBufferTexture(),
                0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
                0, 0, 1, 1
        );
        spriteBatch.end();
        Gdx.gl.glStencilMask(0xFF);

        // 收尾工作
        Gdx.gl.glDisable(GL_STENCIL_TEST);
    }

    protected void renderVirtualEntities(PerspectiveCamera camera, Map<Entity, VirtualEntityInfo> virtualEntities) {
        for (Map.Entry<Entity, VirtualEntityInfo> entry : virtualEntities.entrySet()) {
            renderSystem.beginBatch(camera);

            VirtualEntityInfo info = entry.getValue();
            List<Render> renders = info.entity.getComponents(Render.class);
            info.calculateTransform();

            info.position.setGlobalTransform(info.virtualTransform);
            for (Render render : renders) {
                render.setTransform(info.position);
                renderSystem.addToBatch(render);
            }

            renderSystem.endBatch();

            info.position.setGlobalTransform(info.realTransform);
            for (Render render : renders) {
                render.setTransform(info.position);
            }
        }
    }

    // ----- Virtual Entities ----- //

    @Config public boolean showInnerVirtualEntities = true;
    @Config public boolean showOuterVirtualEntities = true;
    @Getter protected final Map<Entity, VirtualEntityInfo> innerVirtualEntities = new HashMap<>();
    @Getter protected final Map<Entity, VirtualEntityInfo> outerVirtualEntities = new HashMap<>();

    public void addInnerVirtualEntity(Entity entity) {
        if (innerVirtualEntities.containsKey(entity)) return;
        VirtualEntityInfo info = infoPool.obtain();
        info.set(entity, true);
        innerVirtualEntities.put(entity, info);
    }

    public void removeInnerVirtualEntity(Entity entity) {
        if (!innerVirtualEntities.containsKey(entity)) return;
        VirtualEntityInfo info = innerVirtualEntities.remove(entity);
        info.clear();
        infoPool.free(info);
    }

    public void addOuterVirtualEntity(Entity entity) {
        if (outerVirtualEntities.containsKey(entity)) return;
        VirtualEntityInfo info = infoPool.obtain();
        info.set(entity, false);
        outerVirtualEntities.put(entity, info);
    }

    public void removeOuterVirtualEntity(Entity entity) {
        if (!outerVirtualEntities.containsKey(entity)) return;
        VirtualEntityInfo info = outerVirtualEntities.remove(entity);
        info.clear();
        infoPool.free(info);
    }

    protected final Pool<VirtualEntityInfo> infoPool = new Pool<>(VirtualEntityInfo::new);

    protected class VirtualEntityInfo {

        protected Entity entity;
        protected Position position;
        protected boolean isInner;

        protected final Matrix4 realTransform = new Matrix4();
        protected final Matrix4 virtualTransform = new Matrix4();
        protected final Matrix4 offsetTransform = new Matrix4();

        protected void set(Entity entity, boolean in) {
            this.entity = entity;
            this.position = entity.getComponent(Position.class);
            this.isInner = in;
            calculateTransform();
        }

        protected void clear() {
            this.entity = null;
            this.position = null;
            this.isInner = false;
        }

        protected void calculateTransform() {
            position.getGlobalTransform(this.realTransform);
            portal.getTransferTransform(offsetTransform, portal.getTargetTransform());
            virtualTransform.set(realTransform);
            if (isInner) {
                virtualTransform.mulLeft(offsetTransform);
            } else {
                Matrix4 tmpM = Matrix4Pool.obtain();
                tmpM.set(offsetTransform);
                tmpM.inv();
                virtualTransform.mulLeft(tmpM);
                Matrix4Pool.free(tmpM);
            }
        }
    }
}
