package com.my.world.module.gltf;

import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.my.world.core.Component;
import com.my.world.core.Entity;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.common.Position;

public abstract class GLTFLight<T extends BaseLight> extends ActivatableComponent implements Component.OnAttachToEntity, Component.OnDetachFromEntity {

    public Position position;

    public abstract T getLight();

    @Override
    public void attachToEntity(Entity entity) {
        this.position = entity.getComponent(Position.class);
    }

    @Override
    public void detachFromEntity(Entity entity) {
        this.position = null;
    }
}
