package com.my.utils.world;

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
