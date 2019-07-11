package com.my.utils.cam;

import com.badlogic.gdx.graphics.Camera;

public class GestureCameraController
        extends com.badlogic.gdx.graphics.g3d.utils.CameraInputController
        implements CameraControllerMultiplexer.CameraController {

    private Camera camera;

    public GestureCameraController(Camera camera) {
        super(camera);
        this.camera = camera;
    }

    @Override
    public void init() {}
}
