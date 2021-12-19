package com.my.utils.world.sys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.System;
import com.my.utils.world.*;
import com.my.utils.world.com.Camera;
import com.my.utils.world.com.Position;

import java.util.*;

public class CameraSystem extends BaseSystem implements EntityListener, System.OnUpdate, System.OnStart {

    private RenderSystem renderSystem;
    private EnvironmentSystem environmentSystem;

    private final List<CameraInner> cameraInners = new LinkedList<>();
    private final List<SkyBoxInner> skyBoxInners = new LinkedList<>();

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(Camera.class);
    }

    @Override
    public void start(World world) {
        renderSystem = world.getSystemManager().getSystem(RenderSystem.class);
        environmentSystem = world.getSystemManager().getSystem(EnvironmentSystem.class);
    }

    @Override
    public void afterEntityAdded(Entity entity) {
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
        updateCameras();
    }

    @Override
    public void afterEntityRemoved(Entity entity) {
        this.cameraInners.removeIf(cameraInner -> cameraInner.entity == entity);
    }

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        List<String> skyBoxes = (List<String>) config.get("skyBoxes");
        for (String skyBox : skyBoxes) {
            addSkyBox(skyBox);
        }
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        Map<String, Object> map = new LinkedHashMap<>();
        List<String> skyBoxes = new ArrayList<>();
        for (SkyBoxInner skyBoxInner : this.skyBoxInners) {
            skyBoxes.add(skyBoxInner.id);
        }
        map.put("skyBoxes", skyBoxes);
        return map;
    }

    @Override
    public void update(float deltaTime) {

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        Gdx.gl.glViewport(0, 0, width, height);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Environment environment = environmentSystem.getEnvironment();

        for (CameraInner cameraInner : cameraInners) {
            setCamera(cameraInner.camera.followType, cameraInner.perspectiveCamera, cameraInner.position.transform);
            Gdx.gl.glViewport(
                    (int) (width * cameraInner.camera.startX),
                    (int) (height * cameraInner.camera.startY),
                    (int) (width * cameraInner.camera.endX - width * cameraInner.camera.startX),
                    (int) (height * cameraInner.camera.endY - height * cameraInner.camera.startY)
            );
            for (SkyBoxInner skyBox : skyBoxInners) {
                if (skyBox.position == null) {
                    skyBox.entity = world.getEntityManager().getEntity(skyBox.id);
                    skyBox.position = skyBox.entity.getComponent(Position.class);
                }
                skyBox.position.transform.setToTranslation(cameraInner.perspectiveCamera.position);
            }
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            renderSystem.render(cameraInner.perspectiveCamera, environment);
        }

        Gdx.gl.glViewport(0, 0, width, height);
    }

    // ----- Custom ----- //

    public void updateCameras() {
        Collections.sort(this.cameraInners);
    }

    public void addSkyBox(String id) {
        SkyBoxInner skyBoxInner = new SkyBoxInner();
        skyBoxInner.id = id;
        this.skyBoxInners.add(skyBoxInner);
    }

    public void removeSkyBox(String id) {
        this.skyBoxInners.removeIf(skyBoxInner -> id.equals(skyBoxInner.id));
    }

    // ----- Private ----- //

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

    // ----- Inner Class ----- //

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

    private static class SkyBoxInner {
        private String id;
        private Entity entity;
        private Position position;
    }

    public enum FollowType {
        A, B, C
    }
}
