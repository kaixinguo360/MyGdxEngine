package com.my.utils.world.sys;

import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityListener;
import com.my.utils.world.System;
import com.my.utils.world.com.Script;

import java.util.List;

public class ScriptSystem extends BaseSystem implements EntityListener, System.OnUpdate, System.OnKeyDown {

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contains(Script.class);
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        List<Script> scriptList = entity.getComponents(Script.class);
        for (Script script : scriptList) {
            if (script instanceof Script.OnInit) {
                ((Script.OnInit) script).init(world, entity);
            }
        }
    }

    @Override
    public void afterEntityRemoved(Entity entity) {

    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : getEntities()) {
            for (Script script : entity.getComponents(Script.class)) {
                if (!script.disabled && script instanceof Script.OnUpdate) {
                    ((Script.OnUpdate) script).update(world, entity);
                }
            }
        }
    }

    @Override
    public void keyDown(int keycode) {
        for (Entity entity : getEntities()) {
            for (Script script : entity.getComponents(Script.class)) {
                if (script instanceof Script.OnKeyDown) {
                    ((Script.OnKeyDown) script).keyDown(world, entity, keycode);
                }
            }
        }
    }
}
