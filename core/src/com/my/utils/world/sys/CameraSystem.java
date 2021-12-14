package com.my.utils.world.sys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityListener;
import com.my.utils.world.World;
import com.my.utils.world.com.Camera;
import com.my.utils.world.com.Position;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CameraSystem extends BaseSystem implements EntityListener {

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(Camera.class);
    }

    @Override
    public void afterAdded(World world) {
        super.afterAdded(world);
        renderSystem = world.getSystemManager().getSystem(RenderSystem.class);
        environmentSystem = world.getSystemManager().getSystem(EnvironmentSystem.class);
    }

    @Override
    public void afterAdded(Entity entity) {
        CameraInner cameraInner = new CameraInner();
        cameraInner.entity = entity;
        cameraInner.camera = entity.getComponent(Camera.class);
        cameraInner.perspectiveCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraInner.perspectiveCamera.far = 2000;
        cameraInner.perspectiveCamera.near = 0.1f;
        cameraInner.perspectiveCamera.position.set(0, 0, 0);
        cameraInner.perspectiveCamera.update();
        cameraInner.position = entity.getComponent(Position.class);
        this.cameraInners.add(cameraInner);
        Collections.sort(this.cameraInners);
        updateCameras();
    }

    @Override
    public void afterRemoved(Entity entity) {
        this.cameraInners.removeIf(cameraInner -> cameraInner.entity == entity);
    }

    private RenderSystem renderSystem;
    private EnvironmentSystem environmentSystem;

    private List<CameraInner> cameraInners = new LinkedList<>();

    public void render() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        Environment environment = environmentSystem.getEnvironment();
        for (CameraInner cameraInner : cameraInners) {
            setCamera(cameraInner.camera.followType, cameraInner.perspectiveCamera, cameraInner.position.transform);
            Gdx.gl.glViewport(
                    (int) (width * cameraInner.camera.startX),
                    (int) (height * cameraInner.camera.startY),
                    (int) (width * cameraInner.camera.endX - width * cameraInner.camera.startX),
                    (int) (height * cameraInner.camera.endY - height * cameraInner.camera.startY)
            );
            world.getEntityManager().getEntity("sky").getComponent(Position.class).transform.setToTranslation(cameraInner.perspectiveCamera.position);
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            renderSystem.render(cameraInner.perspectiveCamera, environment);
        }
    }
    public void updateCameras() {
        Collections.sort(this.cameraInners);
    }

    private static final Vector3 tmpV1 = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();
    private static final Quaternion tmpQ = new Quaternion();
    private static void setCamera(FollowType type, PerspectiveCamera camera, Matrix4 transform) {
        switch (type) {
            case A: {
                camera.position.set(0, 0.8f, -1.5f).mul(transform);
                camera.direction.set(0, 0, -1).rot(transform);
                camera.up.set(0, 1, 0).rot(transform);
                camera.update();
                break;
            }
            case B: {
                transform.getTranslation(tmpV1);
                float angle = transform.getRotation(tmpQ).getAngleAround(Vector3.Y);
                tmpM.setToTranslation(tmpV1).rotate(Vector3.Y, angle).translate(0, 0, 20);
                camera.position.setZero().mul(tmpM);
                camera.lookAt(transform.getTranslation(tmpV1).add(0, 0, 0));
                camera.up.set(0, 1, 0);
                camera.update();
            }
        }
    }

    private static class CameraInner implements Comparable<CameraInner> {
        private Entity entity;
        private Camera camera;
        private Position position;
        private PerspectiveCamera perspectiveCamera;

        @Override
        public int compareTo(CameraInner o) {
            return this.camera.layer - o.camera.layer;
        }
    }

    public enum FollowType {
        A, B, C;
    }
}
