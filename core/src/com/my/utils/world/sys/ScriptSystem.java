package com.my.utils.world.sys;

import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityListener;
import com.my.utils.world.World;
import com.my.utils.world.com.ScriptComponent;

public class ScriptSystem extends BaseSystem implements EntityListener {

    public boolean isHandleable(Entity entity) {
        return entity.contain(ScriptComponent.class);
    }

    public void update() {
        for (Entity entity : getEntities()) {
            ScriptComponent scriptComponent = entity.getComponent(ScriptComponent.class);
            scriptComponent.script.execute(world, entity, scriptComponent);
        }
    }

    @Override
    public void afterAdded(Entity entity) {
        ScriptComponent scriptComponent = entity.getComponent(ScriptComponent.class);
        scriptComponent.script.init(world, entity, scriptComponent);
    }

    @Override
    public void afterRemoved(Entity entity) {

    }

    public interface Script {
        void init(World world, Entity entity, ScriptComponent scriptComponent);
        void execute(World world, Entity entity, ScriptComponent scriptComponent);
    }

}
