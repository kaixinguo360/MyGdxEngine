package com.my.utils.world.sys;

import com.my.utils.world.System;
import com.my.utils.world.*;
import com.my.utils.world.com.Script;

public class ScriptSystem extends BaseSystem implements StandaloneResource, EntityListener, System.AfterAdded, System.OnUpdate {

    @Override
    protected boolean isHandleable(Entity entity) {
        return entity.contain(OnStart.class) || entity.contain(OnUpdate.class);
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        for (OnStart script : entity.getComponents(OnStart.class)) {
            script.start(world, entity);
        }
    }

    @Override
    public void afterEntityRemoved(Entity entity) {

    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : getEntities()) {
            for (OnUpdate script : entity.getComponents(OnUpdate.class)) {
                script.update(world, entity);
            }
        }
    }

    public interface OnStart extends Script {
        void start(World world, Entity entity);
    }

    public interface OnUpdate extends Script {
        void update(World world, Entity entity);
    }
}
