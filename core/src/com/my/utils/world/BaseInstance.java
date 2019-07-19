package com.my.utils.world;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseInstance implements Instance {

    // ----- Add & Remove ----- //
    protected final Map<String, Component> components = new HashMap<>();
    public void addComponent(String name, Component component) {
        components.put(name, component);
        if (modifyListener != null) modifyListener.add(component);
    }
    public boolean containComponent(Component component) {
        return components.containsValue(component);
    }
    public void removeComponent(String name) {
        if (modifyListener != null) modifyListener.remove(getComponent(name));
        components.remove(name);
    }

    // ----- Get ----- //
    public Component getComponent(String name) {
        return components.get(name);
    }
    public Iterable<Component> getAllComponents() {
        return components.values();
    }

    // ----- Modify Listener ----- //
    private ModifyListener modifyListener = null;
    public void setModifyListener(ModifyListener modifyListener) {
        this.modifyListener = modifyListener;
    }
    public ModifyListener getModifyListener() {
        return modifyListener;
    }
}
