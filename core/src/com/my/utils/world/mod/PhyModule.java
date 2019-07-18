package com.my.utils.world.mod;

import com.badlogic.gdx.utils.Pool;
import com.my.utils.world.BaseModule;

public class PhyModule extends BaseModule<PhyComponent> implements Pool.Poolable {

    // ----- Constructor ----- //
    public static final Pool<PhyModule> pool = new Pool<PhyModule>() {
        @Override
        protected PhyModule newObject() {
            return new PhyModule();
        }
    };
    private PhyModule() {}
    public void reset() {
        modifyListener = null;
        instances.clear();
    }

    // ----- Add & Remove ----- //
    public void add(String name, PhyComponent component) {
        super.add(name, component);
        if (modifyListener != null) modifyListener.add(component);
    }
    public void remove(String name) {
        if (modifyListener != null) modifyListener.remove(get(name));
        super.remove(name);
    }

    // ----- Modify Listener ----- //
    private ModifyListener modifyListener = null;
    void setModifyListener(ModifyListener modifyListener) {
        this.modifyListener = modifyListener;
    }
    static abstract class ModifyListener {
        public abstract void add(PhyComponent component);
        public abstract void remove(PhyComponent component);
    }
}
