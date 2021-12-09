package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class SystemManager implements Disposable {

    @Getter
    private final Map<Class<?>, System> systems = new HashMap<>();

    public <T extends System> T addSystem(T system) {
        Class<? extends System> type = system.getClass();
        if (systems.containsKey(type)) throw new RuntimeException("Duplicate System: " + type);
        systems.put(type, system);
        return system;
    }
    public <T extends System> T removeSystem(Class<T> type) {
        if (!systems.containsKey(type)) throw new RuntimeException("No Such System: " + type);
        return (T) systems.remove(type);
    }
    public <T extends System> T getSystem(Class<T> type) {
        if (!systems.containsKey(type)) throw new RuntimeException("No Such System: " + type);
        return (T) systems.get(type);
    }

    @Override
    public void dispose() {
        for (System system : systems.values()) {
            system.dispose();
        }
        systems.clear();
    }
}
