package com.my.world.enhanced.physics;

import com.my.world.core.Entity;

public abstract class AntiShakeCollisionHandlerAdapter<T extends AntiShakeCollisionHandler.OverlappedEntityInfo> extends AntiShakeCollisionHandler<T> {

    @Override
    protected void onTouch(Entity entity, T info) {

    }

    @Override
    protected void onDetach(Entity entity, T info) {

    }

    @Override
    protected void onEnter(Entity entity, T info) {

    }

    @Override
    protected void onLeave(Entity entity, T info) {

    }

    @Override
    protected void onOverlap(Entity entity, T info) {

    }
}
