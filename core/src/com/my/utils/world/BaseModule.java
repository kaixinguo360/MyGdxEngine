package com.my.utils.world;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseModule<T> implements Module {

    // ----- Add & Remove ----- //
    protected final Map<String, T> instances = new HashMap<>();
    public void add(String name, T component) {
        instances.put(name, component);
    }
    public boolean contain(T component) {
        return instances.containsValue(component);
    }
    public void remove(String name) {
        instances.remove(name);
    }

    // ----- Get ----- //
    public T get(String name) {
        return instances.get(name);
    }
    public Iterable<T> getAll() {
        return instances.values();
    }
}
