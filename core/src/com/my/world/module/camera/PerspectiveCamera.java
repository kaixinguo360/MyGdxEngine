package com.my.world.module.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Config;
import com.my.world.core.Configurable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PerspectiveCamera extends BaseCamera implements Configurable, Camera {

    @Config @Getter public float startX;
    @Config @Getter public float startY;

    @Config @Getter public float endX;
    @Config @Getter public float endY;

    @Config @Getter public int layer;

    @Config(name = "camera", fields = { "far", "near", "up", "direction", "fieldOfView", "viewportWidth", "viewportHeight" })
    public final com.badlogic.gdx.graphics.PerspectiveCamera camera = new com.badlogic.gdx.graphics.PerspectiveCamera();

    public PerspectiveCamera(float startX, float startY, float endX, float endY, int layer) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.layer = layer;
        this.camera.far = 2000;
        this.camera.near = 0.1f;
        this.camera.position.set(0, 0, 0);
        this.camera.fieldOfView = 67;
        this.camera.viewportWidth = Gdx.graphics.getWidth();
        this.camera.viewportHeight = Gdx.graphics.getHeight();
    }

    @Override
    public com.badlogic.gdx.graphics.Camera getCamera() {
        Matrix4 transform = position.getGlobalTransform();
        camera.position.setZero().mul(transform);
        camera.direction.set(0, 0, -1).rot(transform);
        camera.up.set(0, 1, 0).rot(transform);
        camera.update();
        return camera;
    }
}
