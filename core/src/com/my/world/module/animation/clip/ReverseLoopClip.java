package com.my.world.module.animation.clip;

import com.my.world.core.Config;

public class ReverseLoopClip extends LoopClip {

    @Config
    public float reverseRatio;

    @Override
    public float getLocalTime(float currentTime) {
        if (period == 0) return super.getLocalTime(currentTime);
        float period = this.period + this.period * this.reverseRatio;
        float t = ((currentTime - start) * scale % period + period) % period;
        if (t > this.period) {
            t = (period - t) / this.reverseRatio;
        }
        return offset + t;
    }
}
