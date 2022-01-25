package com.my.world.module.common;

import com.my.world.core.System;
import com.my.world.core.*;

import java.util.Collection;

public abstract class BaseSystem implements System, System.AfterAdded {

    protected Scene scene;
    private EntityFilter entityFilter;

    @Override
    public void afterAdded(Scene scene) {
        this.scene = scene;
        this.entityFilter = BaseSystem.this::canHandle;
        scene.getEntityManager().addFilter(entityFilter);
        if (this instanceof EntityListener) {
            scene.getEntityManager().addListener(entityFilter, (EntityListener) this);
        }
    }

    // ----- Entities ----- //
    protected Collection<Entity> getEntities() {
        return scene.getEntityManager().getEntitiesByFilter(entityFilter);
    }
    protected abstract boolean canHandle(Entity entity);
}
