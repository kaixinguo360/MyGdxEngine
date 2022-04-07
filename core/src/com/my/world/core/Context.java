package com.my.world.core;

import com.my.world.core.util.Disposable;
import com.my.world.core.util.Pool;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Context implements Disposable {

    @Getter
    @Setter
    private Context parent;

    private final Map<String, Object> environment = new HashMap<>();

    private Context() {}

    public boolean contains(String id) {
        if (environment.containsKey(id)) {
            return true;
        } else {
            if (parent != null) {
                return parent.contains(id);
            } else {
                return false;
            }
        }
    }

    public <T> T set(String id, T value) {
        if (id == null) throw new RuntimeException("Environment variable id can not be null");
        Object originalValue = environment.get(id);
        if (value == null) {
            environment.remove(id);
        } else {
            environment.put(id, value);
        }
        return (T) originalValue;
    }

    public <T> T get(String id, Class<T> type) {
        if (environment.containsKey(id)) {
            Object value = environment.get(id);
            if (value == null) throw new RuntimeException("Environment variable return value can not be null: id=" + id + ", type=" + type);
            return type.cast(value);
        } else {
            if (parent == null) throw new RuntimeException("No such environment variable: id=" + id + ", type=" + type);
            return parent.get(id, type);
        }
    }

    public <T> T get(String id, Class<T> type, T defaultValue) {
        if (environment.containsKey(id)) {
            Object value = environment.get(id);
            if (value == null) {
                return defaultValue;
            }
            return type.cast(value);
        } else {
            if (parent == null)  {
                return defaultValue;
            }
            return parent.get(id, type, defaultValue);
        }
    }

    public void clear() {
        environment.clear();
    }

    public Context subContext() {
        return obtain(this);
    }

    public <T> T subContext(Function<Context, T> fun) {
        Context subContext = subContext();
        T result = fun.apply(subContext);
        subContext.dispose();
        return result;
    }

    @Override
    public void dispose() {
        this.parent = null;
        this.environment.clear();
        pool.free(this);
    }

    private static final Pool<Context> pool = new Pool<>(Context::new);
    public static Context obtain(Context parent) {
        Context obj = pool.obtain();
        obj.setParent(parent);
        return obj;
    }
}
