package com.my.world.module.animation.clip;

import com.my.world.core.Config;
import com.my.world.core.Configurable;
import com.my.world.module.animation.Animation;
import com.my.world.module.animation.Curve;
import com.my.world.module.animation.Playable;

/**
 * Time Range:
 * <pre>start ~ end</pre>
 * <p>
 * Local Time (time send to <code>playable</code>):
 * <pre>offset + (currentTime - start) * scale</pre>
 */
public abstract class BaseClip implements Playable, Configurable {

    /**
     * Base on global time
     */
    @Config
    public float start = 0;

    /**
     * Base on global time
     */
    @Config
    public float end = -1;

    /**
     * Base on local time
     */
    @Config
    public float offset = 0;

    /**
     * Base on local time
     */
    @Config
    public float scale = 1;

    @Config
    public Curve<Float> weights;

    public boolean canPlay(float currentTime) {
        return (start <= currentTime) && (end == -1 || currentTime <= end);
    }

    public float getWeightsTime(float currentTime) {
        return (currentTime - start);
    }

    public float getLocalTime(float currentTime) {
        return offset + (currentTime - start) * scale;
    }

    public abstract void updateInner(float localTime, float localWeights, Animation animation);

    @Override
    public void update(float currentTime, float weights, Animation animation) {
        if (!canPlay(currentTime)) return;
        float localTime = getLocalTime(currentTime);
        float weightsTime = getWeightsTime(currentTime);
        float localWeights = weights * (this.weights != null ? this.weights.valueAt(weightsTime) : 1);
        updateInner(localTime, localWeights, animation);
    }
}
