package com.my.world.module.render.script;

import com.badlogic.gdx.Gdx;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.script.ScriptSystem;

public class EnhancedThirdPersonCameraController extends SimpleThirdPersonCameraController implements ScriptSystem.OnUpdate {

    @Config public float waitTime = 3f;

    @Config public boolean yawRecoverEnable = true;
    @Config public float yawTarget = 0;
    @Config public float yawRecoverRate = 1f;

    @Config public boolean pitchRecoverEnable = true;
    @Config public float pitchTarget = 0;
    @Config public float pitchRecoverRate = 1f;

    @Config public boolean distanceRecoverEnable = true;
    @Config public float distanceTarget = 20;
    @Config public float distanceRecoverRate = 1f;

    protected float lastYaw;
    protected float lastPitch;
    protected float lastDistance;
    protected float alreadyWaitTime;

    @Override
    public void update(Scene scene, Entity entity) {
        if (lastYaw == yaw && lastPitch == pitch && lastDistance == distance) {
            alreadyWaitTime += Gdx.graphics.getDeltaTime();
            if (alreadyWaitTime > waitTime) {
                updateStatus();
            }
        } else {
            alreadyWaitTime = 0;
            syncStatus();
        }
    }

    public void flushStatus() {
        alreadyWaitTime = waitTime;
        syncStatus();
    }

    protected void updateStatus() {
        if (yawRecoverEnable) {
            yaw = (yaw + 180) % 360 - 180;
            yaw -= (yaw - yawTarget) * 0.02f * yawRecoverRate;
        }
        if (pitchRecoverEnable) {
            pitch -= (pitch - pitchTarget) * 0.02f * pitchRecoverRate;
        }
        if (distanceRecoverEnable) {
            distance -= (distance - distanceTarget) * 0.02f * distanceRecoverRate;
        }
        syncStatus();
        updateTransform();
    }

    protected void syncStatus() {
        lastYaw = yaw;
        lastPitch = pitch;
        lastDistance = distance;
    }
}
