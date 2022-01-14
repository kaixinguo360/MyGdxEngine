package com.my.world.core;

public abstract class ActivatableRelation<T extends ActivatableRelation<T>> extends Relation<T> {

    @Config
    protected boolean activeSelf = true;

    public void setActive(boolean active) {
        if (active != this.activeSelf) {
            this.activeSelf = active;
            cascadeNotifyChange();
        }
    }

    public boolean isActiveInHierarchy() {
        if (activeSelf) {
            if (this.parent != null) {
                return parent.isActiveInHierarchy();
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public boolean isActiveSelf() {
        return activeSelf;
    }
}
