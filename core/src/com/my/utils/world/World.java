package com.my.utils.world;

import com.badlogic.gdx.utils.Pool;

import java.util.HashMap;
import java.util.Map;

public class World {

    // ----- Modify Listener ----- //
    private final Pool<ModifyListener> modifyListenerPool = new Pool<ModifyListener>() {
        @Override
        protected ModifyListener newObject() {
            return new ModifyListener();
        }
    } ;

    // ----- Instance ----- //
    private final Map<String, Instance> instances = new HashMap<>();
    public void addInstance(String name, Instance instance) {
        if (instances.containsKey(name)) throw new RuntimeException("Duplicate instance name: " + name);
        // Instances
        instances.put(name, instance);
        // Component
        for (Component component : instance.getAllComponents()) {
            getModule(component).add(component, name);
        }
        // ModifyListener
        instance.setModifyListener(modifyListenerPool.obtain().set(name));
    }
    public Instance removeInstance(String name) {
        if (!instances.containsKey(name)) throw new RuntimeException("No such instance name: " + name);
        Instance instance = instances.get(name);
        // ModifyListener
        ModifyListener listener = (ModifyListener) instance.getModifyListener();
        instance.setModifyListener(null);
        modifyListenerPool.free(listener);
        // Component
        for (Component component : instance.getAllComponents()) {
            getModule(component).remove(component);
        }
        instances.remove(name);
        // Instances
        return instance;
    }
    public Instance getInstance(String name) {
        return instances.get(name);
    }
    public Instance getInstance(Component component) {
        for (Module module : modules.values()) {
            if (module.handle(component)) {
                String instanceName = module.get(component);
                return (instanceName == null) ? null : instances.get(instanceName);
            }
        }
        return null;
    }

    // ----- Module ----- //
    private final Map<String, Module> modules = new HashMap<>();
    public void addModule(String name, Module module) {
        if (modules.containsKey(name)) throw new RuntimeException("Duplicate name: " + name);
        modules.put(name, module);
    }
    public void removeModule(String name) {
        if (!modules.containsKey(name)) throw new RuntimeException("No such name: " + name);
        modules.remove(name);
    }
    public Module getModule(String name) {
        return modules.get(name);
    }
    public Module getModule(Component component) {
        for (Module module : modules.values()) {
            if (module.handle(component)) return module;
        }
        throw new RuntimeException("Can't Handle This Component: " + component.getClass().getName());
    }


    class ModifyListener implements Instance.ModifyListener, Pool.Poolable {
        private String name;
        private ModifyListener set(String name) {
            this.name = name;
            return this;
        }
        @Override
        public void reset() {
            name = null;
        }
        @Override
        public void add(Component component) {
            getModule(component).add(component, name);
        }
        @Override
        public void remove(Component component) {
            getModule(component).remove(component);
        }
    }
}
