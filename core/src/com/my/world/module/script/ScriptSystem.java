package com.my.world.module.script;

import com.my.world.core.System;
import com.my.world.core.*;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.common.Script;

public class ScriptSystem extends BaseSystem implements EntityListener, System.AfterAdded, System.OnUpdate {

    @Override
    protected boolean isHandleable(Entity entity) {
        return entity.contain(OnStart.class) || entity.contain(OnUpdate.class);
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        for (OnStart script : entity.getComponents(OnStart.class)) {
            script.start(scene, entity);
        }
    }

    @Override
    public void afterEntityRemoved(Entity entity) {

    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : getEntities()) {
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
}
