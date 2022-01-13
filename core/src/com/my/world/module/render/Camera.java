package com.my.world.module.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import com.my.world.module.common.ActivatableComponent;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Camera extends ActivatableComponent implements Loadable {

    @Config public float startX;
    @Config public float startY;

    @Config public float endX;
    @Config public float endY;

    @Config public int layer;

    @Config(name = "camera", fields = { "far", "near", "up", "direction", "fieldOfView", "viewportWidth", "viewportHeight" })
    public final PerspectiveCamera perspectiveCamera = new PerspectiveCamera();

    public Camera(float startX, float startY, float endX, float endY, int layer) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.layer = layer;
        this.perspectiveCamera.far = 2000;
        this.perspectiveCamera.near = 0.1f;
        this.perspectiveCamera.position.set(0, 0, 0);
        this.perspectiveCamera.fieldOfView = 67;
        this.perspectiveCamera.viewportWidth = Gdx.graphics.getWidth();
        this.perspectiveCamera.viewportHeight = Gdx.graphics.getHeight();
    }
}
