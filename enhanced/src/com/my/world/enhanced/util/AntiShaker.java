package com.my.world.enhanced.util;

import com.my.world.core.util.Pool;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class AntiShaker<E> {

    protected final Map<E, Info<E>> touched = new HashMap<>();
    protected final List<Info<E>> confirmed = new ArrayList<>();

    public float confirmThreshold = 3;
    public Function<E, Boolean> filter;
    public Consumer<E> onTouch;
    public Consumer<E> onDetach;
    public Consumer<E> onEnter;
    public Consumer<E> onLeave;
    public Consumer<E> onOverlap;

    // ----- Input ----- //

    public Info<E> touch(E object) {
        if (!filter(object)) return null;
        Info<E> info = touched.get(object);
        if (info == null) {
            info = infoPool.obtain();
            info.set(object);
            touched.put(object, info);
            info.touched = true;
            onTouch(object);
        } else {
            info.touched = true;
        }
        return info;
    }

    public void enter(E object) {
        Info<E> info = touch(object);
        if (info == null) return;
        if (!info.confirmed) {
            info.touchCount = confirmThreshold;
            info.confirmed = true;
            onEnter(object);
        } else {
            info.touchCount = confirmThreshold;
        }
    }

    public Info<E> leave(E object) {
        if (!filter(object)) return null;
        Info<E> info = touched.remove(object);
        if (info == null) return null;
        info.touched = false;
        if (info.confirmed) {
            info.confirmed = false;
            onLeave(info.object);
        }
        return info;
    }

    public void detach(E object) {
        Info<E> info = leave(object);
        if (info == null) return;
        onDetach(info.object);
        info.clear();
        infoPool.free(info);
    }

    protected boolean filter(E object) {
        return filter == null || filter.apply(object);
    }

    // ----- Update ----- //

    public void update() {
        update(1);
    }

    public void update(float step) {
        // Invoke onEnter callback
        for (Info<E> info : touched.values()) {
            if (info.touched) {
                info.touched = false;
                info.touchCount += step;
            } else {
                info.touchCount -= step;
            }
            if (info.touchCount >= confirmThreshold) {
                info.touchCount = confirmThreshold;
                if (!info.confirmed) {
                    info.confirmed = true;
                    onEnter(info.object);
                }
            }
            if (info.confirmed) {
                confirmed.add(info);
            }
        }
        // Invoke onOverlap callback
        for (Info<E> info : confirmed) {
            onOverlap(info.object);
        }
        confirmed.clear();
        // Invoke onLeave callback
        Iterator<Map.Entry<E, Info<E>>> it = touched.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<E, Info<E>> entry = it.next();
            Info<E> info = entry.getValue();
            if (info.touchCount <= 0) {
                it.remove();
                if (info.confirmed) {
                    info.confirmed = false;
                    onLeave(info.object);
                }
                onDetach(info.object);
                info.clear();
                infoPool.free(info);
            }
        }
    }

    // ----- Callback ----- //

    protected void onTouch(E object) {
        if (onTouch != null) {
            onTouch.accept(object);
        }
    }

    protected void onDetach(E object) {
        if (onDetach != null) {
            onDetach.accept(object);
        }
    }

    protected void onEnter(E object) {
        if (onEnter != null) {
            onEnter.accept(object);
        }
    }

    protected void onLeave(E object) {
        if (onLeave != null) {
            onLeave.accept(object);
        }
    }

    protected void onOverlap(E object) {
        if (onOverlap != null) {
            onOverlap.accept(object);
        }
    }

    // ----- Output ----- //

    public <T extends Collection<E>> T getTouchedObjects(T out) {
        this.touched.values().stream().map(i -> i.object).forEachOrdered(out::add);
        return out;
    }

    public <T extends Collection<E>> T getConfirmedObjects(T out) {
        this.confirmed.stream().map(i -> i.object).forEachOrdered(out::add);
        return out;
    }

    public int getTouchedSize() {
        return touched.size();
    }

    public int getConfirmedSize() {
        return confirmed.size();
    }

    public boolean isTouchedEmpty() {
        return touched.isEmpty();
    }

    public boolean isConfirmedEmpty() {
        return confirmed.isEmpty();
    }

    public void clear() {
        for (Info<E> info : touched.values()) {
            info.clear();
            infoPool.free(info);
        }
        touched.clear();
        for (Info<E> info : confirmed) {
            info.clear();
            infoPool.free(info);
        }
        confirmed.clear();
    }

    // ----- Pool ----- //

    protected final Pool<Info<E>> infoPool = new Pool<>(Info<E>::new);

    protected static class Info<E> {

        public E object;
        public float touchCount = 0;
        public boolean touched = false;
        public boolean confirmed = false;

        public void set(E object) {
            this.object = object;
            this.touchCount = 0;
            this.touched = false;
            this.confirmed = false;
        }

        public void clear() {
            this.object = null;
            this.touchCount = 0;
            this.touched = false;
            this.confirmed = false;
        }

    }
}
