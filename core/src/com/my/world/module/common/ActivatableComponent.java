package com.my.world.module.common;

import com.my.world.core.Component;
import com.my.world.core.Config;

public class ActivatableComponent implements Component, Component.Activatable {

    @Config
    protected boolean active = true;

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
