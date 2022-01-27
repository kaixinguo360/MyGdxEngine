package com.my.demo.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Prefab;
import com.my.world.core.Scene;
import com.my.world.core.util.StateManager;
import com.my.world.module.camera.Camera;
import com.my.world.module.camera.script.EnhancedThirdPersonCameraController;
import com.my.world.module.input.InputSystem;
import com.my.world.module.script.ScriptSystem;

public class AircraftScript extends EmitterScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate, InputSystem.OnKeyDown {

    private AircraftController aircraftController_L;
    private AircraftController aircraftController_R;
    private AircraftController aircraftController_T;
    private AircraftController aircraftController_VL;
    private AircraftController aircraftController_VR;

    @Config public Prefab bulletPrefab;
    @Config public Vector3 bulletVelocity = new Vector3(0, 0, -2000);
    private final static Vector3 bulletOffset = new Vector3(0, 0, -5);

    @Config public Prefab bombPrefab;
    @Config public Vector3 bombVelocity = new Vector3(0, -10, 0);
    private final static Vector3 bombOffset = new Vector3(0, -2, 0);

    private final StateManager<State> stateManager = new StateManager<>(State.Idle);

    public enum State {
        Idle, PreFire, Firing, PreBomb, Bombing
    }

    public AircraftScript() {
        stateManager.whenEnter(State.Idle, () -> System.out.println(" -> Idle"));
        stateManager.whenEnter(State.PreFire, () -> System.out.println(" -> PreFire"));
        stateManager.whenEnter(State.Firing, () -> System.out.println(" -> Firing"));
        stateManager.whenEnter(State.PreBomb, () -> System.out.println(" -> PreBomb"));
        stateManager.whenEnter(State.Bombing, () -> System.out.println(" -> Bombing"));

        stateManager.whenEnter(State.Bombing, () -> fire(bombPrefab, bombVelocity, AircraftScript.bombOffset, (float) Math.random()));
        stateManager.whenEnter(State.Firing, () -> fire(bulletPrefab, bulletVelocity, bulletOffset, (float) Math.random()));
    }

    @Override
    public void start(Scene scene, Entity entity) {
        super.start(scene, entity);

        main = entity.findChildByName("body");

        Entity rotate_L = entity.findChildByName("rotate_L");
        Entity rotate_R = entity.findChildByName("rotate_R");
        Entity rotate_T = entity.findChildByName("rotate_T");
        Entity wing_VL = entity.findChildByName("wing_VL");
        Entity wing_VR = entity.findChildByName("wing_VR");
        parts.add(rotate_L);
        parts.add(rotate_R);
        parts.add(rotate_T);
        parts.add(wing_VL);
        parts.add(wing_VR);
        parts.add(entity.findChildByName("engine"));
        parts.add(entity.findChildByName("wing_L1"));
        parts.add(entity.findChildByName("wing_L2"));
        parts.add(entity.findChildByName("wing_R1"));
        parts.add(entity.findChildByName("wing_R2"));
        parts.add(entity.findChildByName("wing_VL"));
        parts.add(entity.findChildByName("wing_VR"));

        if (rotate_L.contains(AircraftController.class))
            aircraftController_L = rotate_L.getComponent(AircraftController.class);
        if (rotate_R.contains(AircraftController.class))
            aircraftController_R = rotate_R.getComponent(AircraftController.class);
        if (rotate_T.contains(AircraftController.class))
            aircraftController_T = rotate_T.getComponent(AircraftController.class);
        if (wing_VL.contains(AircraftController.class))
            aircraftController_VL = wing_VL.getComponent(AircraftController.class);
        if (wing_VR.contains(AircraftController.class))
            aircraftController_VR = wing_VR.getComponent(AircraftController.class);
        Entity cameraEntity = entity.findChildByName("camera");
        if (cameraEntity != null) {
            camera = cameraEntity.getComponent(Camera.class);
            cameraController = cameraEntity.getComponent(EnhancedThirdPersonCameraController.class);
        }
    }

    @Override
    public void update(Scene scene, Entity entity) {
        float v1 = 1f;
        float v2 = 0.5f;
        if (aircraftController_L != null && aircraftController_R != null) {

            if (Gdx.input.isKeyPressed(Input.Keys.J)) {
                stateManager.switchState(State.Idle, State.PreFire);
                stateManager.switchState(State.PreFire, 1000, State.Firing);
                stateManager.switchState(State.Firing, State.Firing);
            } else {
                stateManager.switchState(State.Firing, 1000, State.PreFire);
                stateManager.switchState(State.PreFire, 1000, State.Idle);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.K)) {
                stateManager.switchState(State.Idle, State.PreBomb);
                stateManager.switchState(State.PreBomb, 1000, State.Bombing);
                stateManager.switchState(State.Bombing, 500, State.Bombing);
            } else {
                stateManager.switchState(State.Bombing, State.PreBomb);
                stateManager.switchState(State.PreBomb, 2000, State.Idle);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.W)) aircraftController_T.rotate(v1);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) aircraftController_T.rotate(-v1);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                aircraftController_L.rotate(v2);
                aircraftController_R.rotate(-v2);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                aircraftController_L.rotate(-v2);
                aircraftController_R.rotate(v2);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) aircraftController_VL.rotate(v1);
            if (Gdx.input.isKeyPressed(Input.Keys.E)) aircraftController_VR.rotate(-v1);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) explode();
    }

    @Override
    public void keyDown(int keycode) {
        if (keycode == Input.Keys.SHIFT_LEFT) switchCameraMode();
//        if (keycode == Input.Keys.K) fire(bombPrefab, bombVelocity, AircraftScript.bombOffset, (float) Math.random());
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
                cameraMode = 2;
                cameraController.translateTarget.set(0, 0, 20);
                cameraController.localPitchTarget = 0;
                cameraController.centerTarget.set(0, 5.8f, -1.5f);
                cameraController.flushStatus();
                break;
            case 2:
                cameraMode = 3;
                cameraController.translateTarget.set(0, 0, 50);
                cameraController.localPitchTarget = 0;
                cameraController.centerTarget.set(0, -20, -1.5f);
                cameraController.flushStatus();
                break;
            case 3:
                cameraMode = 0;
                cameraController.translateTarget.set(0, 0, 20);
                cameraController.localPitchTarget = 0;
                cameraController.centerTarget.set(0, 5.8f, -1.5f);
                cameraController.flushStatus();
                break;
        }
    }

}
