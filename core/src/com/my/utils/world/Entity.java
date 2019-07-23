package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

public abstract class Entity implements Disposable {

    // ----- Components ----- //
    protected final Map<Class, Component> components = new HashMap<>();

    // ----- Add & Remove & Get & Contain ----- //
    public <T extends Component> T add(Class<T> type, T component) {
        if (component == null) return null;
        if (components.containsKey(type)) throw new RuntimeException("Duplicate Component: " + type);
        components.put(type, component);
        return component;
    }
    public <T extends Component> T remove(Class<T> type) {
        return (T) components.remove(type);
    }
    public <T extends Component> T get(Class<T> type) {
        return (T) components.get(type);
    }
    public boolean contain(Class... types) {
        for (Class type : types) {
            if (!components.containsKey(type)) return false;
        }
        return true;
    }
}
