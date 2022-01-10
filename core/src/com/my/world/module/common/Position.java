package com.my.world.module.common;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class Position extends BaseComponent {

    @Config
    private Matrix4 localTransform;

    @Getter
    @Setter
    @Config
    private boolean disableInherit = false;

    public Position(Matrix4 localTransform) {
        this.localTransform = localTransform;
    }

    public Matrix4 getLocalTransform() {
        return localTransform;
    }

    public void setLocalTransform(Matrix4 transform) {
        localTransform.set(transform);
    }

    private static final Matrix4 tmpM = new Matrix4();
    public Matrix4 getGlobalTransform() {
        if (disableInherit || entity.getParent() == null) {
            return localTransform;
        } else {
            return getGlobalTransform(tmpM);
        }
    }

    private Matrix4 getGlobalTransform(Matrix4 transform) {
        if (entity == null) throw new RuntimeException("This position component not attached to any entity: " + this);
        if (disableInherit || entity.getParent() == null) {
            transform.set(localTransform);
        } else {
            Entity parent = entity.getParent();
            Position position = parent.getComponent(Position.class);
            if (position == null) throw new RuntimeException("No location component in parent entity: id=" + parent.getId());
            position.getGlobalTransform(transform);
            transform.mul(localTransform);
        }
        return transform;
    }

    public void setGlobalTransform(Matrix4 transform) {
        if (entity == null) throw new RuntimeException("This position component not attached to any entity: " + this);
        // TODO ...
        throw new RuntimeException("Not Implemented");
    }
}
