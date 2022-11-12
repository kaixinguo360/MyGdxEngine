package com.my.world.enhanced.util;

import com.my.world.core.Entity;
import com.my.world.core.EntityManager;
import com.my.world.core.Scene;
import com.my.world.module.script.ScriptSystem;

public class EntitySwitcher extends Switcher<Entity> implements ScriptSystem.OnStart {

    protected EntityManager entityManager;

    @Override
    public void start(Scene scene, Entity entity) {
        entityManager = scene.getEntityManager();
        init();
    }

    @Override
    protected Entity getItem(String name) {
        return entityManager.findEntityByName(name);
    }

    @Override
    protected void enableItem(Entity item) {
        item.setActive(true);
    }

    @Override
    protected void disableItem(Entity item) {
        item.setActive(false);
    }
}
