package com.my.utils.cam;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

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

    private final Vector3 direction = new Vector3();
    private final Vector3 tmp = new Vector3();
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        direction.set(camera.direction);
        this.touchDragged(screenX, screenY, 0);
        tmp.set(direction);
        tmp.y = 0;
        if (tmp.dot(camera.direction) < 0) {
            camera.direction.set(direction);
        }
        return true;
    }
}
