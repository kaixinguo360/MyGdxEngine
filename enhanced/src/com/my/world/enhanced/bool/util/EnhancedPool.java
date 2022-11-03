package com.my.world.enhanced.bool.util;

import com.my.world.core.util.Disposable;
import com.my.world.core.util.Pool;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class EnhancedPool<T> extends Pool<T> implements Disposable {

    @Setter
    @Getter
    private int size = 500;
    private int currentSize = 0;

    private final List<T> managedObjects = new LinkedList<>();

    public EnhancedPool(Supplier<T> supplier) {
        super(supplier);
    }

    @Override
    public T obtain() {
        T object = super.obtain();
        managedObjects.add(object);
        if (currentSize > 0) currentSize--;
        return object;
    }

    @Override
    public void free(T obj) {
        if (currentSize < size) {
            currentSize++;
            super.free(obj);
        }
    }

    @Override
    public void dispose() {
        for (T object : managedObjects) {
            Disposable.dispose(object);
            free(object);
        }
        managedObjects.clear();
    }
}
