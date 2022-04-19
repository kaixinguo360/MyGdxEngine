package com.my.world.module.animation;

@FunctionalInterface
public interface Curve<T> {
    T valueAt(float t);
}
