package com.my.world.module.script;

import com.my.world.core.System;
import com.my.world.core.*;
import com.my.world.module.common.Script;

public class ScriptSystem implements EntityListener, System.AfterAdded, System.OnUpdate {

    protected Scene scene;
    protected final EntityFilter entityListenerFilter = entity -> entity.contain(OnStart.class) || entity.contain(OnRemoved.class);
    protected final EntityFilter onUpdateFilter = entity -> entity.contain(OnUpdate.class);

    @Override
    public void afterAdded(Scene scene) {
        this.scene = scene;
        scene.getEntityManager().addListener(entityListenerFilter, this);
        scene.getEntityManager().addFilter(onUpdateFilter);
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        for (OnStart script : entity.getComponents(OnStart.class)) {
            script.start(scene, entity);
        }
    }

    @Override
    public void afterEntityRemoved(Entity entity) {
        for (OnRemoved script : entity.getComponents(OnRemoved.class)) {
            script.removed(scene, entity);
        }
    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : scene.getEntityManager().getEntitiesByFilter(onUpdateFilter)) {
            for (OnUpdate script : entity.getComponents(OnUpdate.class)) {
                if (Component.isActive(script)) {
                    script.update(scene, entity);
                }
            }
        }
    }

    public interface OnStart extends Script {
        void start(Scene scene, Entity entity);
    }

    public interface OnUpdate extends Script {
        void update(Scene scene, Entity entity);
    }

    public interface OnRemoved extends Script {
        void removed(Scene scene, Entity entity);
    }
}
