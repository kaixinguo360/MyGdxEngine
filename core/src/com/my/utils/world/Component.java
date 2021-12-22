package com.my.utils.world;

public interface Component extends Loadable {

    interface OnAttachToEntity {
        void attachToEntity(Entity entity);
    }

    interface OnDetachFromEntity {
        void detachFromEntity(Entity entity);
    }

}
