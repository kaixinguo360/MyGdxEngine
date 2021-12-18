package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class SystemManager implements Disposable {

    @Getter
    private final Map<Class<?>, System> systems = new LinkedHashMap<>();

    private final World world;

    public SystemManager(World world) {
        this.world = world;
    }

    public <T extends System> T addSystem(T system) {
        Class<? extends System> type = system.getClass();
        if (systems.containsKey(type)) throw new RuntimeException("Duplicate System: " + type);
        systems.put(type, system);
        if (system instanceof System.AfterAdded) ((System.AfterAdded) system).afterAdded(world);
        return system;
    }
    public <T extends System> T removeSystem(Class<T> type) {
        if (!systems.containsKey(type)) throw new RuntimeException("No Such System: " + type);
        T removed = (T) systems.remove(type);
        if (removed instanceof System.AfterRemoved) ((System.AfterRemoved) removed).afterRemoved(world);
        return removed;
    }
    public <T extends System> T getSystem(Class<T> type) {
        if (!systems.containsKey(type)) throw new RuntimeException("No Such System: " + type);
        return (T) systems.get(type);
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
