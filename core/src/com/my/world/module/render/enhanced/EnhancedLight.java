package com.my.world.module.render.enhanced;

import com.my.world.core.Component;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.module.common.BaseComponent;
import com.my.world.module.common.Position;

public class EnhancedLight extends BaseComponent implements Component.Activatable, Component.OnAttachToEntity, Component.OnDetachFromEntity {

    @Config protected boolean active = true;
    @Override public void setActive(boolean active) { this.active = active; }
    @Override public boolean isActive() { return active; }

    public Position position;

    @Override
    public void attachToEntity(Entity entity) {
        super.attachToEntity(entity);
        this.position = entity.getComponent(Position.class);
    }

    @Override
    public void detachFromEntity(Entity entity) {
        super.detachFromEntity(entity);
        this.position = null;
    }
}
