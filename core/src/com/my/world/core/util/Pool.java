package com.my.world.core.util;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class Pool<T> {

    private final Supplier<T> supplier;
    private final List<T> objects = new LinkedList<>();

    public Pool(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public synchronized T obtain() {
        if (!objects.isEmpty()) {
            return objects.remove(0);
        } else {
            return supplier.get();
        }
    }

    public synchronized void free(T obj) {
        if (obj == null) throw new RuntimeException("object cannot be null");
        objects.add(obj);
    }

    public synchronized void clear() {
        objects.clear();
    }
}
