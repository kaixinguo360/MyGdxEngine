package com.my.world.enhanced.depthmask;

import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.render.Render;
import com.my.world.module.script.ScriptSystem;

import java.util.HashMap;
import java.util.Map;

public class CollisionDepthMaskScript extends DepthMaskScript implements ScriptSystem.OnUpdate, PhysicsSystem.OnCollision {

    public final Map<Render, Position> commonHiddenEntities = new HashMap<>();

    @Override
    public void collision(Entity entity) {
        Position position = entity.getComponent(Position.class);
        for (Render render : entity.getComponents(Render.class)) {
            addHiddenRender(render, position);
        }
    }

    @Override
    public void update(Scene scene, Entity entity) {
        clearHiddenRender();
        for (Map.Entry<Render, Position> entry : commonHiddenEntities.entrySet()) {
            addHiddenRender(entry.getKey(), entry.getValue());
        }
    }
}
