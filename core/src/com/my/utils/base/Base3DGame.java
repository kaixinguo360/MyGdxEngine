package com.my.utils.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.my.utils.cam.CameraControllerMultiplexer;
import com.my.utils.cam.FirstPersonCameraController;
import com.my.utils.cam.GestureCameraController;

public abstract class Base3DGame extends BaseGame {

    protected ModelBatch batch;
    protected PerspectiveCamera cam;
    protected CameraControllerMultiplexer cameraControllerMultiplexer;
    @Override
    public void create() {
        super.create();
        // ----- Create batch ----- //
        batch = new ModelBatch();
        addDisposable(batch);
        // ----- Init Camera ----- //
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.far = 500;
        cam.near = 0.001f;
        cam.position.set(0, 0, 0);
        cam.update();
        // ----- Init CameraControllerMultiplexer ----- //
        cameraControllerMultiplexer = new CameraControllerMultiplexer();
        cameraControllerMultiplexer.addProcessor("Gesture", new GestureCameraController(cam), false);
        cameraControllerMultiplexer.addProcessor("First", new FirstPersonCameraController(cam), true);
        cameraControllerMultiplexer.change("Gesture");
        inputMultiplexer.addProcessor(cameraControllerMultiplexer);
    }
}
