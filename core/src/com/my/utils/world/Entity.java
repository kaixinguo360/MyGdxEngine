package com.my.utils.world;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class Entity implements Loadable {

    @Getter
    @Setter
    @Config
    private String id;

    @Getter
    @Setter
    @Config
    private String name;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private boolean handled;

    // ----- Components ----- //
    @Getter
    @Config(elementType = Component.class)
    protected final List<Component> components = new LinkedList<>();

    @Getter
    protected final Map<Class<?>, Component> cache1 = new LinkedHashMap<>();
    protected final Map<Class<?>, List<Component>> cache2 = new HashMap<>();

    // ----- Add & Remove & Get & Contain ----- //
    public <T extends Component> T addComponent(T component) {
        if (component == null) return null;
        components.add(component);
        notifyChange();
        return component;
    }
    public <T extends Component> void removeComponent(Class<T> type) {
        components.removeIf(type::isInstance);
        notifyChange();
    }
    public <T extends Component> T getComponent(Class<T> type) {
        Component cached = cache1.get(type);
        if (cached != null) {
            return (T) cached;
        } else {
            for (Component component : components) {
                if (type.isInstance(component)) {
                    cache1.put(type, component);
                    return (T) component;
                }
            }
            cache1.put(type, null);
            return null;
        }
    }
    public <T extends Component> List<T> getComponents(Class<T> type) {
        List<Component> cached = cache2.get(type);
        if (cached != null) {
            return (List<T>) cached;
        } else {
            List<Component> list = new ArrayList<>();
            for (Component component : this.components) {
                if (type.isInstance(component)) {
                    list.add(component);
                }
            }
            cache2.put(type, list);
            return (List<T>) list;
        }
    }
    public boolean contain(Class<?> type) {
        if (!cache1.containsKey(type)) getComponent((Class<? extends Component>) type);
        return !(cache1.get(type) == null);
    }
    public boolean contain(Class<?>... types) {
        for (Class<?> type : types) {
            if (!contain(type)) {
                return false;
            }
        }
        return true;
    }
    public boolean contains(Class<? extends Component> type) {
        if (!cache2.containsKey(type)) getComponents(type);
        return !cache2.get(type).isEmpty();
    }

    private void notifyChange() {
        handled = false;
        cache1.clear();
        cache2.clear();
    }
}
