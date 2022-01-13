package com.my.world.core;

public abstract class ActivatableRelation<T extends ActivatableRelation<T>> extends Relation<T> {

    @Config
    protected boolean activeSelf = true;

    public void setActive(boolean active) {
        if (active != this.activeSelf) {
            this.activeSelf = active;
            this.changed = true;
        }
    }

    public boolean isActiveInHierarchy() {
        if (this.parent != null) {
            return activeSelf && parent.activeSelf;
        } else {
            return activeSelf;
        }
    }

    public boolean isActiveSelf() {
        return activeSelf;
    }
}
