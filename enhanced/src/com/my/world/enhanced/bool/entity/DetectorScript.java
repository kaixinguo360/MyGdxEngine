package com.my.world.enhanced.bool.entity;

import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.util.AntiShaker;
import com.my.world.module.physics.script.CollisionHandler;
import com.my.world.module.script.ScriptSystem;

import java.util.Collection;
import java.util.function.Function;

public class DetectorScript extends CollisionHandler implements ScriptSystem.OnUpdate {

    @Config
    public Function<Entity, Boolean> filter;

    protected AntiShaker<Entity> antiShaker = new AntiShaker<>();

    @Override
    public void start(Scene scene, Entity entity) {
        super.start(scene, entity);
        antiShaker.filter = filter;
    }

    public void clear() {
        antiShaker.clear();
    }

    public int size() {
        return antiShaker.getTouchedSize();
    }

    public boolean isEmpty() {
        return antiShaker.isTouchedEmpty();
    }

    public <T extends Collection<Entity>> T getEntities(T out) {
        return antiShaker.getTouchedObjects(out);
    }

    @Override
    public void collision(Entity entity) {
        antiShaker.touch(entity);
    }

    @Override
    public void update(Scene scene, Entity entity) {
        antiShaker.update();
    }
}
