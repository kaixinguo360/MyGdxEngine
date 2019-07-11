package com.my.utils.cam;

import com.badlogic.gdx.graphics.Camera;

public class FirstPersonCameraController
        extends com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController
        implements CameraControllerMultiplexer.CameraController {

    private Camera camera;

    public FirstPersonCameraController(Camera camera) {
        super(camera);
        this.camera = camera;
    }

    @Override
    public void init() {
        camera.up.set(0, 1, 0);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return this.touchDragged(screenX, screenY, 0);
    }
}
