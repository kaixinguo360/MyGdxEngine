package com.my.world.module.animation.clip;

import com.my.world.core.Config;
import com.my.world.module.animation.Animation;
import com.my.world.module.animation.Playable;

import java.util.ArrayList;
import java.util.List;

public class ClipGroup extends BaseClip {

    @Config(elementType = Playable.class)
    public List<Playable> playables = new ArrayList<>();

    @Override
    public void updateInner(float localTime, float localWeights, Animation animation) {
        for (Playable playable : playables) {
            playable.update(localTime, localWeights, animation);
        }
    }
}
