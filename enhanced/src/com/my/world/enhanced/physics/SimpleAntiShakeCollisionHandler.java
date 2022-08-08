package com.my.world.enhanced.physics;

public class SimpleAntiShakeCollisionHandler extends AntiShakeCollisionHandlerAdapter<AntiShakeCollisionHandler.OverlappedEntityInfo> {

    @Override
    protected OverlappedEntityInfo newInfo() {
        return new OverlappedEntityInfo();
    }
}
