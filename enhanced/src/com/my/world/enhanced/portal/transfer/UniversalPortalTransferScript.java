package com.my.world.enhanced.portal.transfer;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.common.Position;

public class UniversalPortalTransferScript extends PortalTransferScript<UniversalPortalTransferScript.CapturedEntityInfo> {

    @Config public float distanceThreshold = 0.1f;

    @Override
    protected void onTouch(Entity entity, CapturedEntityInfo info) {
        calcDirection(info.direction, info).nor();
    }

    @Override
    public boolean needTransfer(CapturedEntityInfo info) {
        Vector3 tmpV = Vector3Pool.obtain();
        boolean bool = info.direction.dot(calcDirection(tmpV, info)) < -distanceThreshold;
        Vector3Pool.free(tmpV);
        return bool;
    }

    protected Vector3 calcDirection(Vector3 out, CapturedEntityInfo info) {
        Vector3 tmpV = Vector3Pool.obtain();

        info.position.getGlobalTransform().getTranslation(tmpV);
        Matrix4 selfTransform = portal.selfPosition.getGlobalTransform();
        selfTransform.getTranslation(out);
        out.sub(tmpV);

        Vector3Pool.free(tmpV);

        return out;
    }

    @Override
    protected CapturedEntityInfo newInfo() {
        return new CapturedEntityInfo();
    }

    public static class CapturedEntityInfo extends PortalTransferScript.OverlappedEntityInfo {

        public Position position;
        public Vector3 direction = new Vector3();

        @Override
        public void set(Entity entity) {
            super.set(entity);
            this.position = entity.getComponent(Position.class);
            this.direction.setZero();
        }

        @Override
        public void clear() {
            super.clear();
            this.position = null;
            this.direction.setZero();
        }

    }
}
