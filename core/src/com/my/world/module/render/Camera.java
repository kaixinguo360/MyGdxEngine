package com.my.world.module.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.my.world.core.Component;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Camera implements Component, Loadable {

    @Config public float startX;
    @Config public float startY;

    @Config public float endX;
    @Config public float endY;

    @Config public int layer;
    @Config public CameraSystem.FollowType followType;

    @Config(name = "camera", fields = { "far", "near", "up", "direction", "fieldOfView", "viewportWidth", "viewportHeight" })
    public final PerspectiveCamera perspectiveCamera = new PerspectiveCamera();

    public Camera(float startX, float startY, float endX, float endY, int layer, CameraSystem.FollowType followType) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.layer = layer;
        this.followType = followType;
        this.perspectiveCamera.far = 2000;
        this.perspectiveCamera.near = 0.1f;
        this.perspectiveCamera.position.set(0, 0, 0);
        this.perspectiveCamera.fieldOfView = 67;
        this.perspectiveCamera.viewportWidth = Gdx.graphics.getWidth();
        this.perspectiveCamera.viewportHeight = Gdx.graphics.getHeight();
    }
}
