package com.my.world.enhanced.util;

import com.badlogic.gdx.math.MathUtils;
import lombok.Setter;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Retarder {

    @Setter
    public float changeTime = 1;

    public final Supplier<Float> timeGetter;
    public final Supplier<Float> valueGetter;
    public final Consumer<Float> valueSetter;

    protected boolean isChanging = false;
    protected float timeSinceChangeBegin = 0;
    protected float fromValue;
    protected float toValue;

    public Retarder(Supplier<Float> valueGetter, Consumer<Float> valueSetter) {
        this(null, valueGetter, valueSetter);
    }

    public Retarder(Supplier<Float> timeGetter, Supplier<Float> valueGetter, Consumer<Float> valueSetter) {
        this.timeGetter = timeGetter;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
    }

    public void setValue(float value) {
        setValue(value, changeTime);
    }

    public void setValue(float value, float changeTime) {
        this.isChanging = true;
        this.changeTime = changeTime;
        this.timeSinceChangeBegin = 0;
        this.fromValue = valueGetter.get();
        this.toValue = value;
    }

    public void update() {
        if (!isChanging) return;
        if (timeGetter == null) throw new RuntimeException("TimeGetter is null");
        update(timeGetter.get());
    }

    public void update(float deltaTime) {
        if (!isChanging) return;
        if (timeSinceChangeBegin < changeTime) {
            timeSinceChangeBegin += deltaTime;
            valueSetter.accept(MathUtils.lerp(fromValue, toValue, timeSinceChangeBegin / changeTime));
        } else {
            isChanging = false;
            valueSetter.accept(toValue);
        }
    }
}
