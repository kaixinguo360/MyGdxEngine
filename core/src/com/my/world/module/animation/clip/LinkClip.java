package com.my.world.module.animation.clip;

import com.my.world.core.Config;
import com.my.world.core.Configurable;
import com.my.world.module.animation.Animation;
import com.my.world.module.animation.AnimationClip;
import com.my.world.module.animation.Playable;

public class LinkClip implements Playable, Configurable {

    @Config(type = Config.Type.Asset)
    public AnimationClip clip;

    @Override
    public void update(float currentTime, float weights, Animation animation) {
        clip.update(currentTime, weights, animation);
    }
}
