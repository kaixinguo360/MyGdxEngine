package com.my.world.module.animation;

import com.my.world.core.Config;
import com.my.world.core.Configurable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConstantCurve<T> implements Curve<T>, Configurable {

    @Config
    private T value;

    public ConstantCurve(T value) {
        this.value = value;
    }

    @Override
    public T valueAt(float t) {
        return value;
    }
}
