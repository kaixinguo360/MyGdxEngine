package com.my.world.module.animation;

import com.my.world.core.Configurable;

public interface AnimationController extends Configurable {

    Instance newInstance(Animation animation);

    default void update(float currentTime, float weights, Animation animation) {
        if (animation.controllerInstance == null) {
            animation.controllerInstance = newInstance(animation);
        }
        animation.controllerInstance.update(currentTime, weights);
    }

    interface Instance {
        void update(float currentTime, float weights);
    }

}
