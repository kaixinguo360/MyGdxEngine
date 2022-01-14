package com.my.world.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

public abstract class Relation<T extends Relation> {

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    protected boolean changed = true;

    @Getter
    @Config(type = Config.Type.Entity)
    protected T parent;

    @Getter
    protected final List<T> children = new LinkedList<>();

    public void addChild(T node) {
        children.add(node);
        node.parent = this;
        node.changed = true;
    }

    public void removeChild(T node) {
        children.remove(node);
        node.parent = null;
        node.changed = true;
    }

    public void clearParent() {
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
    }

    public void setParent(T node) {
        clearParent();
        node.addChild(this);
    }

    public T findChildByName(String name) {
        if (name == null) throw new RuntimeException("Name of node can not be null");
        for (T node : children) {
            if (name.equals(node.getName())) {
                return node;
            }
            T child = (T) node.findChildByName(name);
            if (child != null) {
                return child;
            }
        }
        return null;
    }

    protected void cascadeNotifyChange() {
        this.changed = true;
        for (T child : children) {
            child.cascadeNotifyChange();
        }
    }

    public abstract String getName();
}
