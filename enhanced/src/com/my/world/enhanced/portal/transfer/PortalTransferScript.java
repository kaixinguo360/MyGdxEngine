package com.my.world.enhanced.portal.transfer;

import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.physics.AntiShakeCollisionHandler;
import com.my.world.enhanced.physics.AntiShakeCollisionHandlerAdapter;
import com.my.world.enhanced.portal.Portal;
import com.my.world.enhanced.portal.render.PortalRenderScript;
import com.my.world.module.script.ScriptSystem;

public abstract class PortalTransferScript<T extends AntiShakeCollisionHandler.OverlappedEntityInfo> extends AntiShakeCollisionHandlerAdapter<T> implements ScriptSystem.OnStart {

    protected Portal portal;

    protected PortalRenderScript selfRenderScript;
    protected PortalTransferScript selfTransferScript;

    protected PortalRenderScript targetRenderScript;
    protected PortalTransferScript targetTransferScript;

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
            targetTransferScript = portal.targetEntity.getComponent(PortalTransferScript.class);
        }
    }

    @Override
    protected void onEnter(Entity entity, T info) {
        System.out.println(portal.selfEntity.getId() + " <- " + entity.getId());
        if (selfRenderScript != null) selfRenderScript.addInnerVirtualEntity(entity);
        if (targetRenderScript != null) targetRenderScript.addOuterVirtualEntity(entity);
    }

    @Override
    protected void onLeave(Entity entity, T info) {
        System.out.println(portal.selfEntity.getId() + " -> " + entity.getId());
        if (selfRenderScript != null) selfRenderScript.removeInnerVirtualEntity(entity);
        if (targetRenderScript != null) targetRenderScript.removeOuterVirtualEntity(entity);
    }

    @Override
    protected void onOverlap(Entity entity, T info) {
        if (needTransfer(info)) {
            transfer(entity);
        }
    }

    protected void transfer(Entity entity) {
        leave(entity);
        portal.transfer(entity);
        if (targetTransferScript != null) {
            targetTransferScript.enter(entity);
        }
    }

    public abstract boolean needTransfer(T info);
}
