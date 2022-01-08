package com.my.world.gdx;

import com.badlogic.gdx.math.Quaternion;
import com.my.world.core.util.Pool;

public class QuaternionPool {

    private static final Pool<Quaternion> pool = new Pool<>(Quaternion::new);

    private QuaternionPool() {}

    public static Quaternion obtain() {
        return pool.obtain();
    }

    public static void free(Quaternion obj) {
        obj.idt();
        pool.free(obj);
    }
}
