package com.my.world.module.common;

import com.my.world.core.Component;
import com.my.world.core.Entity;

public class BaseComponent implements Component, Component.OnAttachToEntity, Component.OnDetachFromEntity {

    protected Entity entity;

    @Override
    public void attachToEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void detachFromEntity(Entity entity) {
        this.entity = null;
    }
}
