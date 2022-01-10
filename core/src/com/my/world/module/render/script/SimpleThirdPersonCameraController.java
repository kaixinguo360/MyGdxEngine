package com.my.world.module.render.script;

import com.badlogic.gdx.Gdx;
import com.my.world.core.Config;
import com.my.world.module.input.InputSystem;

public class SimpleThirdPersonCameraController extends ThirdPersonCameraController implements InputSystem.OnScrolled, InputSystem.OnMouseMoved {

    @Config public float yawRate = 0.5f;
    @Config public boolean yawLimit = false;
    @Config public float yawMin = -180;
    @Config public float yawMax = 180;

    @Config public float pitchRate = 0.5f;
    @Config public boolean pitchLimit = true;
    @Config public float pitchMin = -90;
    @Config public float pitchMax = 90;

    @Config public float distanceRate = 1f;
    @Config public boolean distanceLimit = true;
    @Config public float distanceMin = 5;
    @Config public float distanceMax = 100;

    @Override
    public void scrolled(int amount) {
        distance += amount * distanceRate;
        if (distanceLimit) distance = Math.max(Math.min(distance, distanceMax), distanceMin);

        updateTransform();
    }

    @Override
    public void mouseMoved(int screenX, int screenY) {
        yaw -= Gdx.input.getDeltaX() * yawRate;
        if (yawLimit) yaw = Math.max(Math.min(yaw, yawMax), yawMin);

        pitch -= Gdx.input.getDeltaY() * pitchRate;
        if (pitchLimit) pitch = Math.max(Math.min(pitch, pitchMax), pitchMin);

        updateTransform();
    }
}
