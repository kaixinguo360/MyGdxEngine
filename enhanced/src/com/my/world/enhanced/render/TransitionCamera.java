package com.my.world.enhanced.render;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.TimeManager;
import com.my.world.enhanced.util.Retarder;
import com.my.world.module.camera.Camera;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.script.ScriptSystem;

public class TransitionCamera extends ActivatableComponent implements Camera, ScriptSystem.OnStart, CameraSystem.BeforeAllRender {

    @Config
    public float transitionTime = 3;

    protected Retarder<com.badlogic.gdx.graphics.Camera> retarder;
    protected Camera fromCamera;
    protected Camera toCamera;
    protected Runnable callback;

    protected final PerspectiveCamera transitionCamera = new PerspectiveCamera();

    @Override
    public void start(Scene scene, Entity entity) {
        TimeManager timeManager = scene.getTimeManager();
        retarder = new Retarder<>(
                timeManager::getRealDeltaTime,
                this::getTransitionCamera,
                this::setTransitionCamera,
                this::lerp
        );
    }

    public void switchCamera(Camera fromCamera, Camera toCamera, Runnable callback) {
        this.fromCamera = fromCamera;
        this.toCamera = toCamera;
        this.callback = callback;
        beforeTransition();
        if (!this.retarder.isChanging()) {
            setTransitionCamera(this.fromCamera.getCamera());
        }
        retarder.setValue(this.toCamera::getCamera, transitionTime);
    }

    @Override
    public void beforeAllRender() {
        if (!retarder.isChanging()) return;
        retarder.update();
        if (!retarder.isChanging()) afterTransition();
    }

    protected void beforeTransition() {
        this.fromCamera.setActive(false);
        this.toCamera.setActive(false);
    }

    protected void afterTransition() {
        this.fromCamera.setActive(false);
        this.fromCamera = null;
        this.toCamera.setActive(true);
        this.toCamera = null;
        if (this.callback != null) {
            this.callback.run();
            this.callback = null;
        }
    }

    protected com.badlogic.gdx.graphics.Camera getTransitionCamera() {
        return transitionCamera;
    }

    protected void setTransitionCamera(com.badlogic.gdx.graphics.Camera camera) {
        if (transitionCamera == camera) return;
        transitionCamera.far = camera.far;
        transitionCamera.near = camera.near;
        if (camera instanceof PerspectiveCamera)
            transitionCamera.fieldOfView = ((PerspectiveCamera) camera).fieldOfView;
        transitionCamera.viewportWidth = camera.viewportWidth;
        transitionCamera.viewportHeight = camera.viewportHeight;
        transitionCamera.position.set(camera.position);
        transitionCamera.direction.set(camera.direction).nor();
        transitionCamera.up.set(camera.up);
        transitionCamera.update();
    }

    protected com.badlogic.gdx.graphics.Camera lerp(com.badlogic.gdx.graphics.Camera c1, com.badlogic.gdx.graphics.Camera c2, float p) {
        transitionCamera.far = MathUtils.lerp(c1.far, c2.far, p);
        transitionCamera.near = MathUtils.lerp(c1.near, c2.near, p);
        if (c1 instanceof PerspectiveCamera && c2 instanceof PerspectiveCamera)
            transitionCamera.fieldOfView = MathUtils.lerp(((PerspectiveCamera) c1).fieldOfView, ((PerspectiveCamera) c2).fieldOfView, p);
        transitionCamera.viewportWidth = MathUtils.lerp(c1.viewportWidth, c2.viewportWidth, p);
        transitionCamera.viewportHeight = MathUtils.lerp(c1.viewportHeight, c2.viewportHeight, p);
        transitionCamera.position.set(c1.position).lerp(c2.position, p);
        transitionCamera.direction.set(c1.direction).lerp(c2.direction, p).nor();
        transitionCamera.up.set(c1.up).lerp(c2.up, p);
        transitionCamera.update();
        return transitionCamera;
    }

    @Override
    public com.badlogic.gdx.graphics.Camera getCamera() {
        if (retarder.isChanging()) {
            return transitionCamera;
        } else {
            return null;
        }
    }

    @Override
    public float getStartX() {
        return toCamera == null ? 0 : toCamera.getStartX();
    }

    @Override
    public float getStartY() {
        return toCamera == null ? 0 : toCamera.getStartY();
    }

    @Override
    public float getEndX() {
        return toCamera == null ? 0 : toCamera.getEndX();
    }

    @Override
    public float getEndY() {
        return toCamera == null ? 0 : toCamera.getEndY();
    }

    @Override
    public int getLayer() {
        return toCamera == null ? 0 : toCamera.getLayer();
    }

    @Override
    public void registerToCameraSystem(Scene scene, Entity entity, CameraSystem cameraSystem) {

    }

    @Override
    public void unregisterFromCameraSystem(Scene scene, Entity entity, CameraSystem cameraSystem) {

    }
}
