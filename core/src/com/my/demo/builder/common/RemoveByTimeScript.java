package com.my.demo.builder.common;

import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.script.ScriptSystem;

public class RemoveByTimeScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate {

    @Config
    public float maxTime = 1;

    private float createTime;

    @Override
    public void start(Scene scene, Entity entity) {
        createTime = scene.getTimeManager().getCurrentTime();
    }

    @Override
    public void update(Scene scene, Entity entity) {
        float timeSinceCreate = scene.getTimeManager().getCurrentTime() - createTime;
        if (timeSinceCreate > maxTime) {
            scene.getEntityManager().getBatch().removeEntity(entity.getId());
        }
    }
}
