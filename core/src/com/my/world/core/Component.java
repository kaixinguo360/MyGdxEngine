package com.my.world.core;

public interface Component extends Loadable {

    interface OnAttachToEntity extends Component {
        void attachToEntity(Entity entity);
    }

    interface OnDetachFromEntity extends Component {
        void detachFromEntity(Entity entity);
    }

    interface Activatable extends Component {
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
