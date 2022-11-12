package com.my.world.enhanced.util;

import com.my.world.core.Config;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class Switcher<E> {

    @Getter
    @Config
    protected int activeIndex = 0;

    @Getter
    protected E activeItem;

    @Config
    protected final List<String> names = new ArrayList<>();

    protected final List<E> items = new ArrayList<>();

    public void add(String name) {
        names.add(name);
    }

    public void remove(String name) {
        remove(names.indexOf(name));
    }

    public void remove(int index) {
        if (index < 0 || index >= names.size() || index >= items.size()) throw new RuntimeException("No such item: index=" + index);
        names.remove(index);
        items.remove(index);
    }

    public void init() {
        if (names.isEmpty()) throw new RuntimeException("Name list is empty");
        for (String name : names) {
            E item = getItem(name);
            if (item == null) throw new RuntimeException("No such item: name=" + name);
            items.add(item);
        }
        activeItem = items.get(activeIndex);
        if (activeItem == null) throw new RuntimeException("No such item: index=" + activeIndex);
        items.forEach(this::disableItem);
        enableItem(this.activeItem);
    }

    public void switchTo(int index) {
        if (index < 0 || index >= items.size()) throw new RuntimeException("No such item: index=" + index);
        E muxE = items.get(index);
        if (muxE == null) throw new RuntimeException("No such item: index=" + index);
        if (activeItem == null) throw new RuntimeException("Active item is null, start() must be called before this method");
        this.activeIndex = index;
        disableItem(this.activeItem);
        this.activeItem = muxE;
        enableItem(this.activeItem);
    }

    public void switchTo(String name) {
        int index = names.indexOf(name);
        if (index == -1) throw new RuntimeException("No such item: name=" + name);
        switchTo(index);
    }

    public void prev() {
        int size = names.size();
        switchTo((activeIndex - 1 + size) % size);
    }

    public void next() {
        int size = names.size();
        switchTo((activeIndex + 1 + size) % size);
    }

    protected abstract E getItem(String name);

    protected abstract void enableItem(E item);

    protected abstract void disableItem(E item);
}
