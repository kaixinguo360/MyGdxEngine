package com.my.utils.world;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    protected final Map<Class<?>, List<Component>> cache = new HashMap<>();

    // ----- Add & Remove & Get & Contain ----- //
    public <T extends Component> T addComponent(T component) {
        if (component == null) return null;
        Class<?> type = component.getClass();
        if (components.containsKey(type)) throw new RuntimeException("Duplicate Component: " + type);
        components.put(type, component);
        handled = false;
        cache.clear();
        return component;
    }
    public <T extends Component> T removeComponent(Class<T> type) {
        handled = false;
        cache.clear();
        return (T) components.remove(type);
    }
    public <T extends Component> void removeComponents(Class<T> type) {
        components.entrySet().removeIf(entry -> type.isAssignableFrom(entry.getKey()));
        handled = false;
        cache.clear();
    }
    public <T extends Component> T getComponent(Class<T> type) {
        return (T) components.get(type);
    }
    public <T extends Component> List<T> getComponents(Class<T> type) {
        if (cache.containsKey(type)) {
            return (List<T>) cache.get(type);
        } else {
            List<Component> list = new ArrayList<>();
            for (Map.Entry<Class<?>, Component> entry : components.entrySet()) {
                if (type.isAssignableFrom(entry.getValue().getClass())) {
                    list.add(entry.getValue());
                }
            }
            cache.put(type, list);
            return (List<T>) list;
        }
    }
    public boolean contain(Class<?>... types) {
        for (Class<?> type : types) {
            if (!components.containsKey(type)) return false;
        }
        return true;
    }
    public boolean contains(Class<? extends Component> type) {
        if (!cache.containsKey(type)) getComponents(type);
        return !cache.get(type).isEmpty();
    }
}
