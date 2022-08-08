package com.my.world.enhanced.portal.transfer;

import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.physics.AntiShakeCollisionHandler;
import com.my.world.enhanced.portal.Portal;
import com.my.world.enhanced.portal.render.PortalRenderScript;
import com.my.world.module.script.ScriptSystem;

public class CollisionPortalTransferScript extends PortalTransferScript<CollisionPortalTransferScript.CapturedEntityInfo> implements ScriptSystem.OnStart{

    protected Portal portal;

    protected PortalRenderScript selfRenderScript;
    protected CollisionPortalTransferScript selfTransferScript;

    protected PortalRenderScript targetRenderScript;
    protected CollisionPortalTransferScript targetTransferScript;

    @Override
    public void start(Scene scene, Entity entity) {
        portal = entity.getComponent(Portal.class);
        if (portal == null) {
            throw new RuntimeException("This entity don't have a Portal component: " + entity.getId());
        }

        selfRenderScript = portal.selfEntity.getComponent(PortalRenderScript.class);
        selfTransferScript = this;

        if (portal.targetPortalName != null) {
            targetRenderScript = portal.targetEntity.getComponent(PortalRenderScript.class);
            targetTransferScript = portal.targetEntity.getComponent(CollisionPortalTransferScript.class);
        }
    }

    @Override
    protected void onLeave(Entity entity, CapturedEntityInfo info) {
        super.onLeave(entity, info);
        if (info.isNeedTransfer) {
            portal.transfer(entity);
            if (targetTransferScript != null) {
                targetTransferScript.enter(entity).isNeedTransfer = false;
            }
        }
    }

    @Override
    public boolean needTransfer(CapturedEntityInfo info) {
        return false;
    }

    @Override
    protected CapturedEntityInfo newInfo() {
        return new CapturedEntityInfo();
    }

    public static class CapturedEntityInfo extends AntiShakeCollisionHandler.OverlappedEntityInfo {

        public boolean isNeedTransfer;

        @Override
        public void set(Entity entity) {
            super.set(entity);
            this.isNeedTransfer = true;
        }

        @Override
        public void clear() {
            super.clear();
            this.isNeedTransfer = false;
        }

    }

}
