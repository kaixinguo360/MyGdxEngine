package com.my.world.module.render;

import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.my.world.core.Entity;
import com.my.world.module.common.BaseComponent;
import com.my.world.module.common.Position;

public abstract class Light extends BaseComponent {

    public Position position;

    public abstract BaseLight getLight();

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
