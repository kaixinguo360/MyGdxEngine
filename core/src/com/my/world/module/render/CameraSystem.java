package com.my.world.module.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Entity;
import com.my.world.core.EntityListener;
import com.my.world.core.Scene;
import com.my.world.core.System;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.common.Position;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CameraSystem extends BaseSystem implements EntityListener, System.OnUpdate, System.OnStart {

    private RenderSystem renderSystem;
    private EnvironmentSystem environmentSystem;

    private final List<CameraInner> cameraInners = new LinkedList<>();

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(Camera.class);
    }

    @Override
    public void start(Scene scene) {
        renderSystem = scene.getSystemManager().getSystem(RenderSystem.class);
        environmentSystem = scene.getSystemManager().getSystem(EnvironmentSystem.class);
    }

    @Override
    public void dispose() {
        renderSystem = null;
        environmentSystem = null;
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

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        Gdx.gl.glViewport(0, 0, width, height);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Environment environment = environmentSystem.getEnvironment();
        Matrix4 tmpM1 = Matrix4Pool.obtain();

        for (CameraInner cameraInner : cameraInners) {
            if (cameraInner.camera.isActive()) {
                setCamera(cameraInner.camera.perspectiveCamera, cameraInner.position.getGlobalTransform());
                Gdx.gl.glViewport(
                        (int) (width * cameraInner.camera.startX),
                        (int) (height * cameraInner.camera.startY),
                        (int) (width * cameraInner.camera.endX - width * cameraInner.camera.startX),
                        (int) (height * cameraInner.camera.endY - height * cameraInner.camera.startY)
                );
                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
                renderSystem.render(cameraInner.camera.perspectiveCamera, environment);
            }
        }

        Matrix4Pool.free(tmpM1);
        Gdx.gl.glViewport(0, 0, width, height);
    }

    // ----- Custom ----- //

    public void updateCameras() {
        Collections.sort(this.cameraInners);
    }

    // ----- Private ----- //

    private static void setCamera(PerspectiveCamera camera, Matrix4 transform) {
        camera.position.setZero().mul(transform);
        camera.direction.set(0, 0, -1).rot(transform);
        camera.up.set(0, 1, 0).rot(transform);
        camera.update();
    }

    // ----- Inner Class ----- //

    private static class CameraInner implements Comparable<CameraInner> {
        private Entity entity;
        private Camera camera;
        private Position position;

        @Override
        public int compareTo(CameraInner o) {
            return this.camera.layer - o.camera.layer;
        }
    }

}
