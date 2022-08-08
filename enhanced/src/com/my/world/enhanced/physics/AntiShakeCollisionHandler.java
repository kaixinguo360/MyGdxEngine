package com.my.world.enhanced.physics;

import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.util.Pool;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.script.ScriptSystem;

import java.util.*;

public abstract class AntiShakeCollisionHandler<T extends AntiShakeCollisionHandler.OverlappedEntityInfo> implements PhysicsSystem.OnCollision, ScriptSystem.OnUpdate {

    protected final Map<Entity, T> overlappedEntities = new HashMap<>();
    private final List<T> confirmedEntities = new ArrayList<>();

    public int overlapThreshold = 3;

    @Override
    public void collision(Entity entity) {
        T info = overlappedEntities.get(entity);
        if (info == null) {
            info = infoPool.obtain();
            info.set(entity);
            overlappedEntities.put(entity, info);
            info.overlapped = true;
            onTouch(entity, info);
        } else {
            info.overlapped = true;
        }
    }

    @Override
    public void update(Scene scene, Entity entity) {

        // Invoke onEnter callback
        for (T info : overlappedEntities.values()) {

            if (info.overlapped) {
                info.overlapped = false;
                info.overlapCount++;
            } else {
                info.overlapCount--;
            }

            if (info.overlapCount >= overlapThreshold) {
                info.overlapCount = overlapThreshold;
                if (!info.confirmed) {
                    info.confirmed = true;
                    onEnter(info.entity, info);
                }
            }

            if (info.confirmed) {
                confirmedEntities.add(info);
            }

        }

        // Invoke onOverlap callback
        for (T info : confirmedEntities) {
            onOverlap(info.entity, info);
        }
        confirmedEntities.clear();

        // Invoke onLeave callback
        Iterator<Map.Entry<Entity, T>> it = overlappedEntities.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Entity, T> entry = it.next();
            T info = entry.getValue();
            if (info.overlapCount <= 0) {
                it.remove();
                if (info.confirmed) {
                    info.confirmed = false;
                    onLeave(info.entity, info);
                }
                onDetach(info.entity, info);
                info.clear();
                infoPool.free(info);
            }
        }

    }

    public T enter(Entity entity) {
        T info = overlappedEntities.get(entity);
        if (info == null) {
            info = infoPool.obtain();
            info.set(entity);
            overlappedEntities.put(entity, info);
            info.overlapped = true;
            onTouch(entity, info);
        }
        if (info.overlapCount < overlapThreshold) {
            info.overlapCount = overlapThreshold;
            info.confirmed = true;
            onEnter(entity, info);
        }
        return info;
    }

    public void leave(Entity entity) {
        T info = overlappedEntities.remove(entity);
        if (info != null) {
            if (info.confirmed) {
                info.confirmed = false;
                onLeave(info.entity, info);
            }
            onDetach(info.entity, info);
            info.clear();
            infoPool.free(info);
        }
    }

    protected abstract void onTouch(Entity entity, T info);

    protected abstract void onDetach(Entity entity, T info);

    protected abstract void onEnter(Entity entity, T info);

    protected abstract void onLeave(Entity entity, T info);

    protected abstract void onOverlap(Entity entity, T info);

    protected abstract T newInfo();

    protected final Pool<T> infoPool = new Pool<>(this::newInfo);

    public static class OverlappedEntityInfo {

        public Entity entity;
        public int overlapCount = 0;
        public boolean overlapped = false;
        public boolean confirmed = false;

        public void set(Entity entity) {
            this.entity = entity;
            this.overlapCount = 0;
            this.overlapped = false;
            this.confirmed = false;
        }

        public void clear() {
            this.entity = null;
            this.overlapCount = 0;
            this.overlapped = false;
            this.confirmed = false;
        }

    }
}
