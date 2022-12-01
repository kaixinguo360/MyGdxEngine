package com.my.world.module.animation;

import com.my.world.core.Configurable;

@FunctionalInterface
public interface Curve<T> extends Configurable {
    T valueAt(float t);
}
