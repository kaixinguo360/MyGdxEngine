package com.my.world.enhanced.portal.transfer;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.enhanced.physics.AntiShakeCollisionHandler;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.common.Position;

public class DirectionalPortalTransferScript extends PortalTransferScript<DirectionalPortalTransferScript.CapturedEntityInfo> {

    @Config public Vector3 direction = new Vector3(0, 0, -1);
    @Config public float distanceThreshold = 0.1f;

    @Override
    protected void onTouch(Entity entity, CapturedEntityInfo info) {
        info.direction = calcDistance(info) > 0 ? 1 : -1;
    }

    @Override
    public boolean needTransfer(CapturedEntityInfo info) {
        return info.direction * calcDistance(info) < -distanceThreshold;
    }

    protected float calcDistance(CapturedEntityInfo info) {

        Vector3 tmpV1 = Vector3Pool.obtain();
        Vector3 tmpV2 = Vector3Pool.obtain();

        info.position.getGlobalTransform().getTranslation(tmpV1);
        Matrix4 selfTransform = portal.selfPosition.getGlobalTransform();
        selfTransform.getTranslation(tmpV2);
        tmpV2.sub(tmpV1);
        tmpV1.set(direction).nor().rot(selfTransform);
        float distance = tmpV1.dot(tmpV2);

        Vector3Pool.free(tmpV1);
        Vector3Pool.free(tmpV2);

        return distance;
    }

    @Override
    protected CapturedEntityInfo newInfo() {
        return new CapturedEntityInfo();
    }

    public static class CapturedEntityInfo extends AntiShakeCollisionHandler.OverlappedEntityInfo {

        public Position position;
        public int direction;

        @Override
        public void set(Entity entity) {
            super.set(entity);
            this.position = entity.getComponent(Position.class);
            this.direction = 0;
        }

        @Override
        public void clear() {
            super.clear();
            this.position = null;
            this.direction = 0;
        }

    }
}
