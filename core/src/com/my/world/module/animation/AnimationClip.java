package com.my.world.module.animation;

import com.my.world.core.Config;
import com.my.world.core.Configurable;

import java.util.ArrayList;
import java.util.List;

public class AnimationClip implements Configurable, Playable {

    @Config(elementType = AnimationChannel.class)
    public List<AnimationChannel> channels = new ArrayList<>();

    @Override
    public void update(float currentTime, float weights, Animation animation) {
        for (AnimationChannel channel : channels) {
            channel.update(currentTime, weights, animation);
        }
    }
}
