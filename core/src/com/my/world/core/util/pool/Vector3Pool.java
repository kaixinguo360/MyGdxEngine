package com.my.world.core.util.pool;

import com.badlogic.gdx.math.Vector3;

public class Vector3Pool {

    private static final Pool<Vector3> pool = new Pool<>(Vector3::new);

    private Vector3Pool() {}

    public static Vector3 obtain() {
        return pool.obtain();
    }

    public static void free(Vector3 obj) {
        obj.setZero();
        pool.free(obj);
    }
}
