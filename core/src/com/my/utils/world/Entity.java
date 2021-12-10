package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public abstract class Entity implements Disposable {

    @Getter
    @Setter
    private String id;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private boolean handled;

    // ----- Components ----- //
    protected final Map<Class<?>, Component> components = new HashMap<>();

    // ----- Add & Remove & Get & Contain ----- //
    public <T extends Component> T addComponent(Class<T> type, T component) {
        if (component == null) return null;
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
