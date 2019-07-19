package com.my.utils.world;

import com.badlogic.gdx.utils.Disposable;

public interface Instance extends Disposable {

    Iterable<Component> getAllComponents();

    void setModifyListener(ModifyListener modifyListener);
    ModifyListener getModifyListener();

    interface ModifyListener {
        void add(Component component);
        void remove(Component component);
    }
}
