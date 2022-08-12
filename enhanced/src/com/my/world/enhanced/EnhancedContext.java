package com.my.world.enhanced;

import com.my.world.core.Context;
import com.my.world.core.util.Pool;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Function;

public class EnhancedContext extends Context {

    @Setter
    @Getter
    protected boolean reverse;

    @Setter
    @Getter
    protected String prefix;

    protected EnhancedContext() {}

    protected boolean reverseContains(String id) {
        if (parent != null) {
            if (parent.contains(id)) {
                return true;
            }
        }
        return environment.containsKey(id);
    }

    protected <T> T reverseGet(String id, Class<T> type) {
        if (parent != null) {
            T value = parent.get(id, type, null);
            if (value != null) return value;
        }
        if (environment.containsKey(id)) {
            Object value = environment.get(id);
            if (value == null) throw new RuntimeException("Environment variable return value can not be null: id=" + id + ", type=" + type);
            return type.cast(value);
        } else {
            throw new RuntimeException("No such environment variable: id=" + id + ", type=" + type);
        }
    }

    protected <T> T reverseGet(String id, Class<T> type, T defaultValue) {
        if (parent != null) {
            T value = parent.get(id, type, null);
            if (value != null) return value;
        }
        if (environment.containsKey(id)) {
            Object value = environment.get(id);
            return type.cast(value);
        } else {
            return defaultValue;
        }
    }

    protected boolean proxyContains(String id) {
        return reverse ? reverseContains(id) : super.contains(id);
    }

    protected <T> T proxyGet(String id, Class<T> type) {
        return reverse ? reverseGet(id, type) : super.get(id, type);
    }

    protected <T> T proxyGet(String id, Class<T> type, T defaultValue) {
        return reverse ? reverseGet(id, type, defaultValue) : super.get(id, type, defaultValue);
    }

    @Override
    public boolean contains(String id) {
//        System.out.println("Contains " + id);
        if (prefix == null || parent == null) return proxyContains(id);
        if (parent.contains(prefix + '.' + id)) {
            return true;
        } else {
            return proxyContains(id);
        }
    }

    @Override
    public <T> T get(String id, Class<T> type) {
//        System.out.println("Get " + id);
        if (prefix == null || parent == null) return proxyGet(id, type);
        T value;
        value = parent.get(prefix + '.' + id, type, null);
        if (value != null) return value;
        return proxyGet(id, type);
    }

    @Override
    public <T> T get(String id, Class<T> type, T defaultValue) {
//        System.out.println("Get " + id + " (" + defaultValue + ")");
        if (prefix == null || parent == null) return proxyGet(id, type, defaultValue);
        T value;
        value = parent.get(prefix + '.' + id, type, null);
        if (value != null) return value;
        return proxyGet(id, type, defaultValue);
    }

    public void copy(String toKey, String fromKey) {
        Object value = get(fromKey, Object.class, null);
        if (value != null) {
            set(toKey, value);
        }
    }

    public void copy(String toKey, String fromKey, Object defaultValue) {
        set(toKey, get(fromKey, Object.class, defaultValue));
    }

    @Override
    public EnhancedContext subContext() {
        return obtain(this);
    }

    public EnhancedContext subContext(String prefix) {
        EnhancedContext subContext = obtain(this);
        subContext.prefix = prefix;
        subContext.reverse = reverse;
        return subContext;
    }

    public <T> T subContext(String prefix, Function<EnhancedContext, T> fun) {
        EnhancedContext subContext = subContext(prefix);
        T result = fun.apply(subContext);
        subContext.dispose();
        return result;
    }

    @Override
    public EnhancedContext clone() {
        EnhancedContext newContext = obtain(this.parent);
        newContext.prefix = prefix;
        newContext.reverse = reverse;
        newContext.environment.putAll(this.environment);
        return newContext;
    }

    @Override
    public void dispose() {
        this.prefix = null;
        this.parent = null;
        this.reverse = false;
        this.environment.clear();
        pool.free(this);
    }

    private static final Pool<EnhancedContext> pool = new Pool<>(EnhancedContext::new);
    public static EnhancedContext obtain(Context parent) {
        EnhancedContext obj = pool.obtain();
        obj.setParent(parent);
        if (parent instanceof EnhancedContext) {
            obj.reverse = ((EnhancedContext) parent).reverse;
        }
        return obj;
    }
}
