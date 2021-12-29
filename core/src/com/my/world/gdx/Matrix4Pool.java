package com.my.world.gdx;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Pool;

public class Matrix4Pool {

    private static final Pool<Matrix4> pool = new Pool<>(Matrix4::new);

    private Matrix4Pool() {}

    public static Matrix4 obtain() {
        return pool.obtain();
    }

    public static void free(Matrix4 obj) {
        obj.idt();
        pool.free(obj);
    }
}
