package com.my.world.module.render.script;

import com.badlogic.gdx.Gdx;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.input.InputSystem;

public class EnhancedThirdPersonCameraController extends SmoothThirdPersonCameraController implements InputSystem.OnScrolled, InputSystem.OnMouseMoved {

    @Config public float waitTime = 3f;

    @Config public boolean yawLocked = false;
    @Config public float yawRate = 1f;
    @Config public boolean yawLimit = false;
    @Config public float yawMin = -180;
    @Config public float yawMax = 180;

    @Config public boolean pitchLocked = false;
    @Config public float pitchRate = 1f;
    @Config public boolean pitchLimit = true;
    @Config public float pitchMin = -90;
    @Config public float pitchMax = 90;

    @Config public float distanceRate = 1f;
    @Config public boolean distanceLimit = true;
    @Config public float distanceMin = 0.001f;
    @Config public float distanceMax = 100;

    protected float alreadyWaitTime;
    protected boolean changed;

    @Override
    public void scrolled(int amount) {
        float distance = translate.len();
        distance *= 1 + amount * 0.1f * distanceRate;
        if (distanceLimit) distance = Math.max(Math.min(distance, distanceMax), distanceMin);
        translate.nor().scl(distance);

        changed = true;

        updateTransform();
    }

    @Override
    public void mouseMoved(int screenX, int screenY) {
        if (!yawLocked) {
            yaw -= Gdx.input.getDeltaX() * 0.1f * yawRate;
            if (yawLimit) yaw = Math.max(Math.min(yaw, yawMax), yawMin);
        }

        if (!pitchLocked) {
            pitch -= Gdx.input.getDeltaY() * 0.1f * pitchRate;
            if (pitchLimit) pitch = Math.max(Math.min(pitch, pitchMax), pitchMin);
        }

        changed = true;

        updateTransform();
    }

    @Override
    public void update(Scene scene, Entity entity) {
        if (recoverEnabled) {
            if (!changed) {
                alreadyWaitTime += Gdx.graphics.getDeltaTime();
                if (alreadyWaitTime > waitTime) {
                    updateStatus();
                }
            } else {
                changed = false;
                alreadyWaitTime = 0;
            }
        }
    }

    public void flushStatus() {
        changed = false;
        alreadyWaitTime = waitTime;
    }
}
