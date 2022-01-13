package com.my.world.module.render.script;

import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.script.ScriptSystem;

public class SmoothThirdPersonCameraController extends ThirdPersonCameraController implements ScriptSystem.OnUpdate {

    @Config public boolean recoverEnabled = true;
    @Config public float recoverRate = 1f;

    @Config public boolean yawRecoverEnabled = true;
    @Config public float yawTarget = 0;
    @Config public float yawRecoverRate = 1f;

    @Config public boolean pitchRecoverEnabled = true;
    @Config public float pitchTarget = 0;
    @Config public float pitchRecoverRate = 1f;

    @Config public boolean localYawRecoverEnabled = true;
    @Config public float localYawTarget = 0;
    @Config public float localYawRecoverRate = 1f;

    @Config public boolean localPitchRecoverEnabled = true;
    @Config public float localPitchTarget = 0;
    @Config public float localPitchRecoverRate = 1f;

    @Config public boolean centerRecoverEnabled = true;
    @Config public Vector3 centerTarget = new Vector3(0, 0, 0);
    @Config public float centerRecoverRate = 1f;

    @Config public boolean translateRecoverEnabled = true;
    @Config public Vector3 translateTarget = new Vector3(0, 0, 20);
    @Config public float translateRecoverRate = 1f;

    @Override
    public void update(Scene scene, Entity entity) {
        if (recoverEnabled) {
            updateStatus();
        }
    }

    protected void updateStatus() {
        if (yawRecoverEnabled) {
            yaw = (yaw + 180) % 360 - 180;
            yaw -= (yaw - yawTarget) * 0.02f * yawRecoverRate * recoverRate;
        }
        if (pitchRecoverEnabled) {
            pitch -= (pitch - pitchTarget) * 0.02f * pitchRecoverRate * recoverRate;
        }
        if (localYawRecoverEnabled) {
            localYaw = (localYaw + 180) % 360 - 180;
            localYaw -= (localYaw - localYawTarget) * 0.02f * localYawRecoverRate * recoverRate;
        }
        if (localPitchRecoverEnabled) {
            localPitch -= (localPitch - localPitchTarget) * 0.02f * localPitchRecoverRate * recoverRate;
        }
        Vector3 tmp = Vector3Pool.obtain();
        if (centerRecoverEnabled) {
            tmp.set(center).sub(centerTarget).scl(0.02f * centerRecoverRate * recoverRate);
            center.sub(tmp);
        }
        if (translateRecoverEnabled) {
            tmp.set(translate).sub(translateTarget).scl(0.02f * translateRecoverRate * recoverRate);
            translate.sub(tmp);
        }
        Vector3Pool.free(tmp);
        updateTransform();
    }
}
