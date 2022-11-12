package com.my.demo.entity.aircraft;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.common.EmitterScript;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.util.EnhancedStateManager;
import com.my.world.enhanced.builder.EntityBuilder;
import com.my.world.enhanced.physics.HingeConstraintController;
import com.my.world.module.camera.PerspectiveCamera;
import com.my.world.module.camera.script.EnhancedThirdPersonCameraController;
import com.my.world.module.input.InputSystem;
import com.my.world.module.script.ScriptSystem;

public class AircraftScript extends EmitterScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate, InputSystem.OnKeyDown {

    private HingeConstraintController rotationController_L;
    private HingeConstraintController rotationController_R;
    private HingeConstraintController rotationController_T;
    private HingeConstraintController rotationController_VL;
    private HingeConstraintController rotationController_VR;

    @Config(type = Config.Type.Asset) public EntityBuilder bulletBuilder;
    @Config public Vector3 bulletVelocity = new Vector3(0, 0, -2000);
    private final static Vector3 bulletOffset = new Vector3(0, 0, -5);

    @Config(type = Config.Type.Asset) public EntityBuilder bombBuilder;
    @Config public Vector3 bombVelocity = new Vector3(0, -10, 0);
    private final static Vector3 bombOffset = new Vector3(0, -2, 0);

    private final EnhancedStateManager<State, Action> stateManager = new EnhancedStateManager<State, Action>(State.Idle) {{
        whenEnter(State.Idle, () -> System.out.println(" -> Idle"));
        whenEnter(State.PreFire, () -> System.out.println(" -> PreFire"));
        whenEnter(State.Firing, () -> System.out.println(" -> Firing"));
        whenEnter(State.PreBomb, () -> System.out.println(" -> PreBomb"));
        whenEnter(State.Bombing, () -> System.out.println(" -> Bombing"));

        whenEnter(State.Firing, () -> fire(bulletBuilder, bulletVelocity, bulletOffset, (float) Math.random()));
        addAction(Action.Fire, State.Idle, State.PreFire);
        addAction(Action.Fire, State.PreFire, 1, State.Firing);
        addAction(Action.Fire, State.Firing, State.Firing);
        addAction(Action.StopFire, State.Firing, 1, State.PreFire);
        addAction(Action.StopFire, State.PreFire, 1, State.Idle);

        whenEnter(State.Bombing, () -> fire(bombBuilder, bombVelocity, AircraftScript.bombOffset, (float) Math.random()));
        addAction(Action.Bomb, State.Idle, State.PreBomb);
        addAction(Action.Bomb, State.PreBomb, 1, State.Bombing);
        addAction(Action.Bomb, State.Bombing, 1, State.Bombing);
        addAction(Action.StopBomb, State.Bombing, State.PreBomb);
        addAction(Action.StopBomb, State.PreBomb, 1, State.Idle);
    }};

    public enum State {
        Idle, PreFire, Firing, PreBomb, Bombing
    }

    public enum Action {
        Fire, Bomb, StopFire, StopBomb
    }

    @Override
    public void start(Scene scene, Entity entity) {
        super.start(scene, entity);

        stateManager.init(scene.getTimeManager());

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

        if (rotate_L.contains(HingeConstraintController.class))
            rotationController_L = rotate_L.getComponent(HingeConstraintController.class);
        if (rotate_R.contains(HingeConstraintController.class))
            rotationController_R = rotate_R.getComponent(HingeConstraintController.class);
        if (rotate_T.contains(HingeConstraintController.class))
            rotationController_T = rotate_T.getComponent(HingeConstraintController.class);
        if (wing_VL.contains(HingeConstraintController.class))
            rotationController_VL = wing_VL.getComponent(HingeConstraintController.class);
        if (wing_VR.contains(HingeConstraintController.class))
            rotationController_VR = wing_VR.getComponent(HingeConstraintController.class);
        Entity cameraEntity = entity.findChildByName("camera");
        if (cameraEntity != null) {
            camera = cameraEntity.getComponent(PerspectiveCamera.class);
            cameraController = cameraEntity.getComponent(EnhancedThirdPersonCameraController.class);
        }
    }

    @Override
    public void update(Scene scene, Entity entity) {
        float deltaTime = scene.getTimeManager().getDeltaTime();
        float v1 = 1f * deltaTime * 50f;
        float v2 = 0.5f * deltaTime * 50f;
        if (rotationController_L != null && rotationController_R != null) {

            if (Gdx.input.isKeyPressed(Input.Keys.J)) {
                stateManager.doAction(Action.Fire);
            } else {
                stateManager.doAction(Action.StopFire);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.K)) {
                stateManager.doAction(Action.Bomb);
            } else {
                stateManager.doAction(Action.StopBomb);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.W)) rotationController_T.rotate(v1);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) rotationController_T.rotate(-v1);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                rotationController_L.rotate(v2);
                rotationController_R.rotate(-v2);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                rotationController_L.rotate(-v2);
                rotationController_R.rotate(v2);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) rotationController_VL.rotate(v1);
            if (Gdx.input.isKeyPressed(Input.Keys.E)) rotationController_VR.rotate(-v1);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) explode();
    }

    @Override
    public void keyDown(int keycode) {
//        if (keycode == Input.Keys.SHIFT_LEFT) switchCameraMode();
//        if (keycode == Input.Keys.K) fire(bombBuilder, bombVelocity, AircraftScript.bombOffset, (float) Math.random());
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
