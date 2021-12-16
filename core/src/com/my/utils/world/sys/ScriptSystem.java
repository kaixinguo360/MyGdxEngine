package com.my.utils.world.sys;

import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityListener;
import com.my.utils.world.com.Script;

import java.util.List;

public class ScriptSystem extends BaseSystem implements EntityListener {

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contains(Script.class);
    }

    @Override
    public void afterAdded(Entity entity) {
        List<Script> scriptList = entity.getComponents(Script.class);
        for (Script script : scriptList) {
            script.init(world, entity);
        }
    }

    @Override
    public void afterRemoved(Entity entity) {

    }

    public void update() {
        for (Entity entity : getEntities()) {
            for (Script script : entity.getComponents(Script.class)) {
                if (!script.disabled) script.execute(world, entity);
            }
        }
    }

}
