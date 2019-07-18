package com.my.utils.world.mod;

import com.badlogic.gdx.utils.Pool;
import com.my.utils.world.BaseModule;

public class ModelModule extends BaseModule<ModelComponent> implements Pool.Poolable {

    // ----- Constructor ----- //
    public static final Pool<ModelModule> pool = new Pool<ModelModule>() {
        @Override
        protected ModelModule newObject() {
            return new ModelModule();
        }
    };
    private ModelModule() {}
    public void reset() {
        instances.clear();
    }

}
