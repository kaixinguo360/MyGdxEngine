package com.my.utils.world;

import java.util.HashMap;
import java.util.Map;

public class World {

    private final Map<String, Instance> instances = new HashMap<>();
    public void addInstance(String name, Instance instance) {
        if (instances.containsKey(name)) throw new RuntimeException("Duplicate instance name: " + name);
        instances.put(name, instance);
        for (Module module : instance.getModules()) {
            getHandler(module).add(module, name);
        }
    }
    public Instance removeInstance(String name) {
        if (!instances.containsKey(name)) throw new RuntimeException("No such instance name: " + name);
        Instance instance = instances.get(name);
        for (Module module : instance.getModules()) {
            getHandler(module).remove(module);
        }
        instances.remove(name);
        return instance;
    }
    public Instance getInstance(String name) {
        return instances.get(name);
    }
    public Iterable<Instance> getInstances() {
        return instances.values();
    }

    private final Map<String, Handler> handlers = new HashMap<>();
    public void addHandler(String name, Handler handler) {
        if (handlers.containsKey(name)) throw new RuntimeException("Duplicate name: " + name);
        handlers.put(name, handler);
    }
    public void removeHandler(String name) {
        if (!handlers.containsKey(name)) throw new RuntimeException("No such name: " + name);
        handlers.remove(name);
    }
    public Handler getHandler(String name) {
        return handlers.get(name);
    }
    public Iterable<Handler> getHandlers() {
        return handlers.values();
    }

    public Handler getHandler(Module module) {
        for (Handler handler : getHandlers()) {
            if (handler.handle(module)) return handler;
        }
        throw new RuntimeException("No Such Module Handler: " + module.getClass().getName());
    }

}
