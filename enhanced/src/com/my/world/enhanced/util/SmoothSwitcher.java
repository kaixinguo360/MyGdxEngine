package com.my.world.enhanced.util;

import com.badlogic.gdx.math.MathUtils;
import com.my.world.core.Config;
import lombok.Getter;

import java.util.function.Supplier;

public abstract class SmoothSwitcher<E> extends Switcher<E> {

    @Config
    public boolean interruptable = false;

    @Config
    public float transitionTime = 0;

    public final Supplier<Float> timeGetter;

    @Getter
    protected boolean isChanging = false;
    protected float timeSinceChangeBegin = 0;
    protected int fromIndex = -1;
    protected E fromItem = null;
    protected int toIndex = -1;
    protected E toItem = null;

    public SmoothSwitcher() {
        this(null);
    }

    public SmoothSwitcher(Supplier<Float> timeGetter) {
        this.timeGetter = timeGetter;
    }

    @Override
    public void switchTo(int index) {
        if (isChanging && !interruptable) return;
        if (index < 0 || index >= items.size()) throw new RuntimeException("No such item: index=" + index);
        E item = items.get(index);
        if (item == null) throw new RuntimeException("No such item: index=" + index);
        if (isChanging) finishTransition();
        isChanging = true;
        timeSinceChangeBegin = 0;
        fromIndex = activeIndex;
        fromItem = activeItem;
        toIndex = index;
        toItem = item;
        beforeDisable(fromItem);
        beforeEnable(toItem);
        if (timeSinceChangeBegin >= transitionTime) finishTransition();
    }

    public void finishTransition() {
        if (isChanging) {
            afterDisable(fromItem);
            afterEnable(toItem);
            isChanging = false;
            activeIndex = toIndex;
            activeItem = toItem;
            fromIndex = -1;
            fromItem = null;
            toIndex = -1;
            toItem = null;
        }
    }

    public void update() {
        if (!isChanging) return;
        if (timeGetter == null) throw new RuntimeException("TimeGetter is null");
        update(timeGetter.get());
    }

    public void update(float deltaTime) {
        if (!isChanging) return;
        if (timeSinceChangeBegin < transitionTime) {
            timeSinceChangeBegin += deltaTime;
            float progress = MathUtils.clamp(timeSinceChangeBegin / transitionTime, 0, 1);
            onDisable(fromItem, 1 - progress);
            onEnable(toItem, progress);
        } else {
            finishTransition();
        }
    }

    // ----- Disable ----- //

    protected void beforeDisable(E item) {
        disableItem(item);
    }

    protected void onDisable(E item, float progress) {
    }

    protected void afterDisable(E item) {
    }

    // ----- Enable ----- //

    protected void beforeEnable(E item) {
    }

    protected void onEnable(E item, float progress) {
    }

    protected void afterEnable(E item) {
        enableItem(item);
    }
}
