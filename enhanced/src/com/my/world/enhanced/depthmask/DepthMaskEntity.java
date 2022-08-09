package com.my.world.enhanced.depthmask;

import com.my.world.core.Entity;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.common.Position;
import com.my.world.module.render.Render;

public class DepthMaskEntity extends EnhancedEntity {

    public final DepthMaskScript depthMaskScript;

    public DepthMaskEntity() {
        setName("DepthMask");
        depthMaskScript = addComponent(new DepthMaskScript());
    }

    // ----- Add & Remove Entity ----- //

    public void addEntity(Entity entity) {
        addEntity(entity, true);
    }

    public void addEntity(Entity entity, boolean recursion) {
        addRendersFromEntity(entity, recursion);

        Position position = entity.getComponent(Position.class);
        if (position != null) {
            position.disableInherit();
            entity.setParent(this);
            position.enableInherit();
        } else {
            entity.setParent(this);
        }
    }

    public void removeEntity(Entity entity) {
        removeEntity(entity, true);
    }

    public void removeEntity(Entity entity, boolean recursion) {
        removeRendersFromEntity(entity, recursion);

        Position position = entity.getComponent(Position.class);
        if (position != null) {
            position.disableInherit();
            entity.clearParent();
            position.enableInherit();
        } else {
            entity.setParent(this);
        }
    }

    // ----- Add & Remove Render Component ----- //

    public void addRendersFromEntity(Entity entity, boolean recursion) {
        Position position = entity.getComponent(Position.class);
        if (position != null) {
            for (Render render : entity.getComponents(Render.class)) {
                depthMaskScript.addHiddenRender(render, position);
            }
        }

        if (recursion) {
            for (Entity child : entity.getChildren()) {
                addRendersFromEntity(child, recursion);
            }
        }
    }

    public void removeRendersFromEntity(Entity entity, boolean recursion) {
        for (Render render : entity.getComponents(Render.class)) {
            depthMaskScript.removeHiddenRender(render);
        }

        if (recursion) {
            for (Entity child : entity.getChildren()) {
                removeRendersFromEntity(child, recursion);
            }
        }
    }
}
