package com.my.utils.world.sys;

import com.my.utils.world.System;
import com.my.utils.world.*;
import com.my.utils.world.com.Script;

public class ScriptSystem implements StandaloneResource, System.AfterAdded, System.OnUpdate, System.OnKeyDown {

    private World world;

    private EntityFilter onOnUpdateFilter;

    private EntityFilter onOnKeyDownFilter;

    @Override
    public void afterAdded(World world) {
        this.world = world;

        onOnUpdateFilter = entity -> entity.contain(Script.OnUpdate.class);
        world.getEntityManager().addFilter(onOnUpdateFilter);

        onOnKeyDownFilter = entity -> entity.contain(Script.OnKeyDown.class);
        world.getEntityManager().addFilter(onOnKeyDownFilter);
    }

    @Override
    public void update(float deltaTime) {
        for (Entity entity : world.getEntityManager().getEntitiesByFilter(onOnUpdateFilter)) {
            for (Script.OnUpdate onUpdateScript : entity.getComponents(Script.OnUpdate.class)) {
                Script script = (Script) onUpdateScript;
                if (!script.running) {
                    if (script instanceof Script.OnStart) {
                        ((Script.OnStart) script).start(world, entity);
                    }
                    script.running = true;
                }
                if (!script.disabled) {
                    onUpdateScript.update(world, entity);
                }
            }
        }
    }

    @Override
    public void keyDown(int keycode) {
        for (Entity entity : world.getEntityManager().getEntitiesByFilter(onOnKeyDownFilter)) {
            for (Script script : entity.getComponents(Script.class)) {
                if (script instanceof Script.OnKeyDown) {
                    ((Script.OnKeyDown) script).keyDown(world, entity, keycode);
                }
            }
        }
    }
}
