package com.my.world.module.common;

import com.my.world.core.Component;
import com.my.world.core.Config;

public abstract class BaseActivatableComponent extends BaseComponent implements Component.Activatable {

    @Config
    protected boolean active = true;

    @Override
    public void setActive(boolean active) {
        if (active == this.active) return;
        this.active = active;
        if (this.entity != null) {
            this.entity.notifyChange();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
