package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SystemManager implements Disposable {

    @Getter
    private final Map<Class<?>, System> systems = new LinkedHashMap<>();
    private final Map<Class<?>, List<System>> cache = new LinkedHashMap<>();

    private final World world;

    public SystemManager(World world) {
        this.world = world;
    }

    public <T extends System> T addSystem(T system) {
        Class<? extends System> type = system.getClass();
        if (systems.containsKey(type)) throw new RuntimeException("Duplicate System: " + type);
        systems.put(type, system);
        notifyChange();
        if (system instanceof System.AfterAdded) ((System.AfterAdded) system).afterAdded(world);
        return system;
    }
    public <T extends System> T removeSystem(Class<T> type) {
        if (!systems.containsKey(type)) throw new RuntimeException("No Such System: " + type);
        T removed = (T) systems.remove(type);
        notifyChange();
        if (removed instanceof System.AfterRemoved) ((System.AfterRemoved) removed).afterRemoved(world);
        return removed;
    }
    public <T extends System> T getSystem(Class<T> type) {
        if (!systems.containsKey(type)) throw new RuntimeException("No Such System: " + type);
        return (T) systems.get(type);
    }
    public <T extends System> List<T> getSystems(Class<T> type) {
        List<System> cached = cache.get(type);
        if (cached != null) {
            return (List<T>) cached;
        } else {
            List<System> list = new ArrayList<>();
            for (System system : systems.values()) {
                if (type.isInstance(system)) {
                    list.add(system);
                }
            }
            cache.put(type, list);
            return (List<T>) list;
        }
    }

    private void notifyChange() {
        cache.clear();
    }

    @Override
    public void dispose() {
        for (System system : systems.values()) {
            if (system instanceof Disposable) {
                ((Disposable) system).dispose();
            }
        }
        systems.clear();
    }
}
