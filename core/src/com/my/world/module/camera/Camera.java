package com.my.world.module.camera;

import com.my.world.core.Component;
import com.my.world.core.Entity;
import com.my.world.core.Scene;

public interface Camera extends Component, Component.Activatable, Comparable<Camera> {

    void registerToCameraSystem(Scene scene, Entity entity, CameraSystem cameraSystem);

    void unregisterFromCameraSystem(Scene scene, Entity entity, CameraSystem cameraSystem);

    float getStartX();

    float getStartY();

    float getEndX();

    float getEndY();

    int getLayer();

    com.badlogic.gdx.graphics.Camera getCamera();

    default int compareTo(Camera other) {
        return this.getLayer() - other.getLayer();
    }

}
