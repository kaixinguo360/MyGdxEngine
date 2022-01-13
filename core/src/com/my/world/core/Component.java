package com.my.world.core;

public interface Component extends Loadable {

    interface OnAttachToEntity {
        void attachToEntity(Entity entity);
    }

    interface OnDetachFromEntity {
        void detachFromEntity(Entity entity);
    }

    interface Activatable {
        void setActive(boolean active);
        boolean isActive();
    }

    static boolean isActive(Component component) {
        if (component instanceof Activatable) {
            return ((Activatable) component).isActive();
        } else {
            return true;
        }
    }

}
