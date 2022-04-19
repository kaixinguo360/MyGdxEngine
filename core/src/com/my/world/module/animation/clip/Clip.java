package com.my.world.module.animation.clip;

import com.my.world.core.Config;
import com.my.world.module.animation.Animation;
import com.my.world.module.animation.Playable;

public class Clip extends BaseClip {

    @Config
    public Playable playable;

    public void updateInner(float localTime, float localWeights, Animation animation) {
        playable.update(localTime, localWeights, animation);
    }
}
