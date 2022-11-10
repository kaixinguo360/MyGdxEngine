package com.my.world.enhanced.render;

import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.camera.Camera;

import java.util.ArrayList;
import java.util.List;

public class CameraGroup extends TransitionCamera {

    @Config
    public boolean global = false;

    @Config
    public int activeCameraIndex = 0;

    @Config
    public final List<String> cameras = new ArrayList<>();

    protected final List<Camera> cameraComponents = new ArrayList<>();
    protected Camera activeCameraComponent;

    @Override
    public void start(Scene scene, Entity entity) {
        super.start(scene, entity);
        if (cameras.isEmpty()) throw new RuntimeException("Camera list is empty");
        for (String cameraName : cameras) {
            Entity cameraEntity = global ? scene.getEntityManager().findEntityByName(cameraName) : entity.findChildByName(cameraName);
            if (cameraEntity == null) throw new RuntimeException("No such entity: name=" + cameraName);
            Camera cameraComponent = cameraEntity.getComponent(Camera.class);
            if (cameraComponent == null) throw new RuntimeException("No camera component found in this entity: id=" + cameraEntity.getId());
            cameraComponents.add(cameraComponent);
        }
        cameraComponents.forEach(c -> c.setActive(false));
        if (activeCameraIndex < 0 || activeCameraIndex >= cameras.size()) throw new RuntimeException("No such camera: index=" + activeCameraIndex);
        activeCameraComponent = cameraComponents.get(activeCameraIndex);
        if (activeCameraComponent == null) throw new RuntimeException("No such camera: index=" + activeCameraIndex);
        activeCameraComponent.setActive(true);
    }

    public void switchCamera(int index) {
        if (index < 0 || index >= cameras.size()) throw new RuntimeException("No such camera: index=" + index);
        Camera camera = cameraComponents.get(index);
        if (camera == null) throw new RuntimeException("No such camera: index=" + index);
        if (activeCameraComponent == null) throw new RuntimeException("Active camera is null, start() must be called before this method");
        this.activeCameraIndex = index;
        this.switchCamera(this.activeCameraComponent, camera, () -> {
            this.activeCameraComponent = camera;
        });
    }

    public void switchCamera(String name) {
        int index = cameras.indexOf(name);
        if (index == -1) throw new RuntimeException("No such camera: name=" + name);
        switchCamera(index);
    }

    public void prevCamera() {
        int size = cameras.size();
        switchCamera((activeCameraIndex - 1 + size) % size);
    }

    public void nextCamera() {
        int size = cameras.size();
        switchCamera((activeCameraIndex + 1 + size) % size);
    }
}
