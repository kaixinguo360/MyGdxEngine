package com.my.world.enhanced.bool.operation;

import com.my.world.core.util.Disposable;
import com.my.world.core.util.Pool;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class EnhancedPool<T> extends Pool<T> implements Disposable {

    private final List<T> allObjects = new LinkedList<>();

    public EnhancedPool(Supplier<T> supplier) {
        super(supplier);
    }

    @Override
    public T obtain() {
        T object = super.obtain();
        allObjects.add(object);
        return object;
    }

    @Override
    public void dispose() {
        for (T object : allObjects) {
            Disposable.dispose(object);
            free(object);
        }
        allObjects.clear();
    }
}
