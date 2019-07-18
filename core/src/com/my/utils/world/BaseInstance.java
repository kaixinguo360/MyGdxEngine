package com.my.utils.world;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseInstance implements Instance {

    // ----- Add & Remove ----- //
    private final Map<String, Module> modules = new HashMap<>();
    protected void addModule(String name, Module module) {
        modules.put(name, module);
    }
    protected void removeModule(String name) {
        modules.remove(name);
    }

    // ----- Get ----- //
    protected <T extends Module> T getModule(String name, Class<T> type) {
        return (T) modules.get(name);
    }
    @Override
    public Iterable<Module> getModules() {
        return modules.values();
    }
}
