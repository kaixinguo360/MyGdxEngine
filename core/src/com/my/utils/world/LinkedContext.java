package com.my.utils.world;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class LinkedContext implements Context {

    @Getter
    @Setter
    private Context parent;

    private final Map<String, Object> environment = new HashMap<>();

    public LinkedContext(Context parent) {
        this.parent = parent;
    }

    @Override
    public boolean containsEnvironment(String id) {
        if (environment.containsKey(id)) {
            return true;
        } else {
            if (parent != null) {
                return parent.containsEnvironment(id);
            } else {
                return false;
            }
        }
    }

    @Override
    public <T> T setEnvironment(String id, T value) {
        if (id == null) throw new RuntimeException("Environment variable id can not be null");
        environment.put(id, value);
        return value;
    }

    @Override
    public <T> T getEnvironment(String id, Class<T> type) {
        if (environment.containsKey(id)) {
            Object value = environment.get(id);
            if (value == null) throw new RuntimeException("Environment variable return value can not be null: id=" + id + ", type=" + type);
            return type.cast(value);
        } else {
            if (parent == null) throw new RuntimeException("No such environment variable: id=" + id + ", type=" + type);
            return parent.getEnvironment(id, type);
        }
    }

    @Override
    public void clearEnvironments() {
        environment.clear();
    }
}
