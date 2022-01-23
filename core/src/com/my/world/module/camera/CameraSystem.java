package com.my.world.module.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.System;
import com.my.world.core.*;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.common.Position;
import com.my.world.module.common.RenderSystem;
import com.my.world.module.common.Script;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CameraSystem extends BaseSystem implements EntityListener, System.OnUpdate, System.OnStart {

    protected RenderSystem renderSystem;

    protected final EntityFilter beforeRenderFilter = entity -> entity.contain(BeforeRender.class);
    protected final EntityFilter afterRenderFilter = entity -> entity.contain(AfterRender.class);
    protected final List<CameraInner> cameraInners = new LinkedList<>();

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(Camera.class);
    }

    @Override
    public void afterAdded(Scene scene) {
        super.afterAdded(scene);
        scene.getEntityManager().addFilter(beforeRenderFilter);
        scene.getEntityManager().addFilter(afterRenderFilter);
    }

    @Override
    public void start(Scene scene) {
        renderSystem = scene.getSystemManager().getSystem(RenderSystem.class);
    }

    @Override
    public void dispose() {
        renderSystem = null;
        for (CameraInner cameraInner : cameraInners) {
            cameraInner.camera = null;
            cameraInner.entity = null;
            cameraInner.position = null;
        }
        cameraInners.clear();
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        Position position = entity.getComponent(Position.class);
        for (Camera camera : entity.getComponents(Camera.class)) {
            CameraInner cameraInner = new CameraInner();
            cameraInner.entity = entity;
            cameraInner.camera = camera;
            cameraInner.position = position;
            this.cameraInners.add(cameraInner);
        }
        updateCameras();
    }

    @Override
    public void afterEntityRemoved(Entity entity) {
        this.cameraInners.removeIf(cameraInner -> cameraInner.entity == entity);
    }

    @Override
    public void update(float deltaTime) {
        renderSystem.begin();

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        for (CameraInner cameraInner : cameraInners) {
            if (cameraInner.camera.isActive()) {
                setCamera(cameraInner.camera.perspectiveCamera, cameraInner.position.getGlobalTransform());
                resetViewport(
                        (int) (width * cameraInner.camera.startX),
                        (int) (height * cameraInner.camera.startY),
                        (int) (width * cameraInner.camera.endX - width * cameraInner.camera.startX),
                        (int) (height * cameraInner.camera.endY - height * cameraInner.camera.startY)
                );
                render(cameraInner.camera.perspectiveCamera);
            }
        }
        resetViewport(0, 0, width, height);

        renderSystem.end();
    }

    // ----- Custom ----- //

    public void updateCameras() {
        Collections.sort(this.cameraInners);
    }

    // ----- Protected ----- //

    protected void render(PerspectiveCamera perspectiveCamera) {
        for (Entity entity : scene.getEntityManager().getEntitiesByFilter(beforeRenderFilter)) {
            for (BeforeRender script : entity.getComponents(BeforeRender.class)) {
                if (Component.isActive(script)) {
                    script.beforeRender(perspectiveCamera);
                }
            }
        }

        renderSystem.render(perspectiveCamera);

        for (Entity entity : scene.getEntityManager().getEntitiesByFilter(afterRenderFilter)) {
            for (AfterRender script : entity.getComponents(AfterRender.class)) {
                if (Component.isActive(script)) {
                    script.afterRender(perspectiveCamera);
                }
            }
        }
    }

    protected static void setCamera(PerspectiveCamera camera, Matrix4 transform) {
        camera.position.setZero().mul(transform);
        camera.direction.set(0, 0, -1).rot(transform);
        camera.up.set(0, 1, 0).rot(transform);
        camera.update();
    }

    protected static void resetViewport(int x, int y, int width, int height) {
        Gdx.gl.glViewport(x, y, width, height);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
    }

    // ----- Inner Class ----- //

    protected static class CameraInner implements Comparable<CameraInner> {
        protected Entity entity;
        protected Camera camera;
        protected Position position;

        @Override
        public int compareTo(CameraInner o) {
            return this.camera.layer - o.camera.layer;
        }
    }

    public interface BeforeRender extends Script {
        void beforeRender(PerspectiveCamera cam);
    }

    public interface AfterRender extends Script {
        void afterRender(PerspectiveCamera cam);
    }

}
