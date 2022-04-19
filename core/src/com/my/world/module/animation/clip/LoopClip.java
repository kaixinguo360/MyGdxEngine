package com.my.world.module.animation.clip;

import com.my.world.core.Config;

public class LoopClip extends Clip {

    /**
     * Base on local time
     */
    @Config
    public float period;

    @Override
    public float getLocalTime(float currentTime) {
        if (period == 0) return super.getLocalTime(currentTime);
        return offset + ((currentTime - start) * scale % period + period) % period;
    }
}
