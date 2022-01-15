package com.my.demo.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Prefab;
import com.my.world.core.Scene;
import com.my.world.module.input.InputSystem;
import com.my.world.module.render.Camera;
import com.my.world.module.render.script.EnhancedThirdPersonCameraController;
import com.my.world.module.script.ScriptSystem;

public class GunScript extends EmitterScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate, InputSystem.OnKeyDown {

    private GunController gunController_Y;
    private GunController gunController_X;

    @Config public Prefab bulletPrefab;
    @Config public Vector3 bulletVelocity = new Vector3(0, 0, -2000);
    private final static Vector3 bulletOffset = new Vector3(0, 0, -5);

    @Config public Prefab bombPrefab;
    @Config public Vector3 bombVelocity = new Vector3(0, 0, -100);
    private final static Vector3 bombOffset = new Vector3(0, 0, -5);

    @Override
    public void start(Scene scene, Entity entity) {
        super.start(scene, entity);

        main = entity.findChildByName("barrel");
        Entity rotate_Y = entity.findChildByName("rotate_Y");
        Entity rotate_X = entity.findChildByName("rotate_X");
        parts.add(rotate_X);
        parts.add(rotate_Y);

        if (rotate_Y.contains(GunController.class)) gunController_Y = rotate_Y.getComponent(GunController.class);
        if (rotate_X.contains(GunController.class)) gunController_X = rotate_X.getComponent(GunController.class);
        Entity cameraEntity = entity.findChildByName("camera");
        if (cameraEntity != null) {
            camera = cameraEntity.getComponent(Camera.class);
            cameraController = cameraEntity.getComponent(EnhancedThirdPersonCameraController.class);
        }
    }

    @Override
    public void update(Scene scene, Entity entity) {
        float v = 0.025f;
        if (gunController_Y != null && gunController_X != null) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                getMainBody().activate();
                gunController_X.target -= v;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                getMainBody().activate();
                gunController_X.target += v;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                getMainBody().activate();
                gunController_Y.target += v;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                getMainBody().activate();
                gunController_Y.target -= v;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.J)) fire(bulletPrefab, bulletVelocity, bulletOffset, (float) Math.random());
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) explode();
    }

    @Override
    public void keyDown(int keycode) {
        if (keycode == Input.Keys.SHIFT_LEFT) switchCameraMode();
        if (keycode == Input.Keys.K) fire(bombPrefab, bombVelocity, bombOffset, (float) Math.random());
    }

    @Config
    private int cameraMode = 0;
    public void switchCameraMode() {
        switch (cameraMode) {
            case 0:
                cameraMode = 1;
                cameraController.translateTarget.set(0, 0, 0.01f);
                cameraController.localPitchTarget = 0;
                cameraController.centerTarget.set(0, 0.8f, -1.5f);
                cameraController.flushStatus();
                break;
            case 1:
                cameraMode = 0;
                cameraController.translateTarget.set(2, 0, 10);
                cameraController.localPitchTarget = 0;
                cameraController.centerTarget.set(0, 2.8f, -1.5f);
                cameraController.flushStatus();
                break;
        }
    }
}
