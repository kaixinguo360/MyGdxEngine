package com.my.world.enhanced.util;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Retarder<E> {

    @Setter
    public float changeTime = 1;

    public final Supplier<Float> timeGetter;
    public final Supplier<E> valueGetter;
    public final Consumer<E> valueSetter;
    public final LerpFunction<E> lerpFunction;

    @Getter
    protected boolean isChanging = false;
    protected float timeSinceChangeBegin = 0;
    protected E fromValue;
    protected E toValue;
    protected Supplier<E> toValueGetter;

    public Retarder(Supplier<E> valueGetter, Consumer<E> valueSetter, LerpFunction<E> lerpFunction) {
        this(null, valueGetter, valueSetter, lerpFunction);
    }

    public Retarder(Supplier<Float> timeGetter, Supplier<E> valueGetter, Consumer<E> valueSetter, LerpFunction<E> lerpFunction) {
        this.timeGetter = timeGetter;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
        this.lerpFunction = lerpFunction;
    }

    public void setValue(E value) {
        setValue(value, changeTime);
    }

    public void setValue(E value, float changeTime) {
        this.isChanging = true;
        this.changeTime = changeTime;
        this.timeSinceChangeBegin = 0;
        this.fromValue = valueGetter.get();
        this.toValue = value;
        this.toValueGetter = null;
    }

    public void setValue(Supplier<E> toValueGetter) {
        setValue(toValueGetter, changeTime);
    }

    public void setValue(Supplier<E> toValueGetter, float changeTime) {
        this.isChanging = true;
        this.changeTime = changeTime;
        this.timeSinceChangeBegin = 0;
        this.fromValue = valueGetter.get();
        this.toValue = null;
        this.toValueGetter = toValueGetter;
    }

    public void update() {
        if (!isChanging) return;
        if (timeGetter == null) throw new RuntimeException("TimeGetter is null");
        update(timeGetter.get());
    }

    public void update(float deltaTime) {
        if (!isChanging) return;
        if (toValueGetter != null) toValue = toValueGetter.get();
        if (timeSinceChangeBegin < changeTime) {
            timeSinceChangeBegin += deltaTime;
            valueSetter.accept(lerpFunction.lerp(fromValue, toValue, timeSinceChangeBegin / changeTime));
        } else {
            isChanging = false;
            valueSetter.accept(toValue);
        }
    }

    public interface LerpFunction<E> {
        E lerp(E fromValue, E toValue, float progress);
    }
}
