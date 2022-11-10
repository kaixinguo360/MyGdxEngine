package com.my.world.module.camera;

import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.common.Position;

public abstract class BaseCamera extends ActivatableComponent implements Camera {

    protected Scene scene;
    protected Entity entity;
    protected CameraSystem cameraSystem;
    protected Position position;

    @Override
    public void registerToCameraSystem(Scene scene, Entity entity, CameraSystem cameraSystem) {
        this.scene = scene;
        this.entity = entity;
        this.cameraSystem = cameraSystem;
        this.position = entity.getComponent(Position.class);
    }

    @Override
    public void unregisterFromCameraSystem(Scene scene, Entity entity, CameraSystem cameraSystem) {
        this.position = null;
        this.cameraSystem = null;
        this.entity = null;
        this.scene = null;
    }
}
