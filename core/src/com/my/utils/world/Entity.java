package com.my.utils.world;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class Entity {

    @Getter
    @Setter
    private String id;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private boolean handled;

    // ----- Components ----- //
    @Getter
    protected final Map<Class<?>, Component> components = new HashMap<>();

    // ----- Add & Remove & Get & Contain ----- //
    public <T extends Component> T addComponent(T component) {
        if (component == null) return null;
        Class<?> type = component.getClass();
        if (components.containsKey(type)) throw new RuntimeException("Duplicate Component: " + type);
        components.put(type, component);
        handled = false;
        return component;
    }
    public <T extends Component> T removeComponent(Class<T> type) {
        handled = false;
        return (T) components.remove(type);
    }
    public <T extends Component> T getComponent(Class<T> type) {
        return (T) components.get(type);
    }
    public boolean contain(Class<?>... types) {
        for (Class<?> type : types) {
            if (!components.containsKey(type)) return false;
        }
        return true;
    }
}
