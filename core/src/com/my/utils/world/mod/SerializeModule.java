package com.my.utils.world.mod;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.SerializationException;
import com.my.utils.world.BaseModule;
import com.my.utils.world.Component;

import java.util.HashMap;
import java.util.Map;

public class SerializeModule extends BaseModule<SerializeComponent> {

    private static final Json JSON = new Json();
    private static final Map<String, String> data = new HashMap<>();

    // ----- Component ----- //
    private final Map<String, SerializeComponent> components = new HashMap<>();
    protected void addComponent(SerializeComponent component) {
        components.put(component.name, component);
    }
    protected void removeComponent(SerializeComponent component) {
        components.remove(component.name);
    }
    public boolean handle(Component component) {
        return component instanceof SerializeComponent;
    }

    // ----- Custom ----- //
    public String serialize() {
        data.clear();
        for (SerializeComponent component : components.values()) {
            data.put(component.name, component.serialize());
        }
        return JSON.toJson(data);
    }
    public void deserialize(String data) {
        try {
            Map<String, String> values = JSON.fromJson(HashMap.class, data);
            for (Map.Entry<String, String> entry : values.entrySet()) {
                SerializeComponent component = components.get(entry.getKey());
                if (component != null) component.deserialize(entry.getValue());
            }
        } catch (SerializationException e) {
            e.printStackTrace();
            return;
        }
    }
}
