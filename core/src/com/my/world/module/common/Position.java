package com.my.world.module.common;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.gdx.Matrix4Pool;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.function.Consumer;

@NoArgsConstructor
public class Position extends BaseComponent {

    @Config
    protected Matrix4 localTransform;

    @Getter
    @Setter
    @Config
    protected boolean disableInherit = false;

    public Position(Matrix4 localTransform) {
        this.localTransform = localTransform;
    }

    public Matrix4 getLocalTransform() {
        return localTransform;
    }

    public Matrix4 getLocalTransform(Matrix4 transform) {
        return transform.set(localTransform);
    }

    public void setLocalTransform(Matrix4 transform) {
        localTransform.set(transform);
    }

    public void setLocalTransform(Consumer<Matrix4> consumer) {
        Matrix4 tmpM = Matrix4Pool.obtain();
        getLocalTransform(tmpM);
        consumer.accept(tmpM);
        setLocalTransform(tmpM);
        Matrix4Pool.free(tmpM);
    }

    private static final Matrix4 tmpM = new Matrix4();
    public Matrix4 getGlobalTransform() {
        if (disableInherit || entity.getParent() == null) {
            return localTransform;
        } else {
            return getGlobalTransform(tmpM);
        }
    }

    public Matrix4 getGlobalTransform(Matrix4 transform) {
        if (disableInherit || entity.getParent() == null) {
            return transform.set(localTransform);
        } else {
            return getGlobalTransformInner(transform);
        }
    }

    private Matrix4 getGlobalTransformInner(Matrix4 transform) {
        if (entity == null) throw new RuntimeException("This position component not attached to any entity: " + this);
        if (disableInherit || entity.getParent() == null) {
            transform.set(localTransform);
        } else {
            Entity parent = entity.getParent();
            Position position = parent.getComponent(Position.class);
            if (position == null) throw new RuntimeException("No location component in parent entity: id=" + parent.getId());
            position.getGlobalTransformInner(transform);
            transform.mul(localTransform);
        }
        return transform;
    }

    public void setGlobalTransform(Matrix4 transform) {
        if (entity == null) throw new RuntimeException("This position component not attached to any entity: " + this);
        if (disableInherit || entity.getParent() == null) {
            setLocalTransform(transform);
        } else {
            Entity parent = entity.getParent();
            Position parentPosition = parent.getComponent(Position.class);
            if (parentPosition == null) {
                setLocalTransform(transform);
                return;
            }
            Matrix4 tmpM = Matrix4Pool.obtain();
            parentPosition.getGlobalTransform(tmpM);
            tmpM.inv().mul(transform);
            setLocalTransform(tmpM);
            Matrix4Pool.free(tmpM);
        }
    }

    public void setGlobalTransform(Consumer<Matrix4> consumer) {
        Matrix4 tmpM = Matrix4Pool.obtain();
        getGlobalTransform(tmpM);
        consumer.accept(tmpM);
        setGlobalTransform(tmpM);
        Matrix4Pool.free(tmpM);
    }

    public void enableInherit() {
        if (entity == null) throw new RuntimeException("This position component not attached to any entity: " + this);
        if (!disableInherit) throw new RuntimeException("This component is already enabled inherit: " + this);
        if (entity.getParent() == null) {
            disableInherit = false;
        } else {
            Entity parent = entity.getParent();
            Position parentPosition = parent.getComponent(Position.class);
            if (parentPosition == null) {
                disableInherit = false;
                return;
            }
            Matrix4 localTransform = getLocalTransform();
            disableInherit = false;
            setGlobalTransform(localTransform);
        }
    }

    public void disableInherit() {
        if (entity == null) throw new RuntimeException("This position component not attached to any entity: " + this);
        if (disableInherit) throw new RuntimeException("This component is already disabled inherit: " + this);
        if (entity.getParent() == null) {
            disableInherit = true;
        } else {
            Entity parent = entity.getParent();
            Position parentPosition = parent.getComponent(Position.class);
            if (parentPosition == null) {
                disableInherit = true;
                return;
            }
            Matrix4 globalTransform = getGlobalTransform();
            disableInherit = true;
            setLocalTransform(globalTransform);
        }
    }
}
