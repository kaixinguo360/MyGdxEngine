package com.my.world.enhanced.bool.entity;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.EntityManager;
import com.my.world.core.Scene;
import com.my.world.enhanced.bool.operation.BooleanOperationException;
import com.my.world.enhanced.bool.util.BooleanUtil;
import com.my.world.enhanced.physics.SimpleAntiShakeCollisionHandler;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.Position;
import com.my.world.module.script.ScriptSystem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class CutterScript extends SimpleAntiShakeCollisionHandler implements ScriptSystem.OnStart {

    @Config(type = Config.Type.Asset)
    public Model cutter;

    @Config
    public BooleanUtil.Type type = BooleanUtil.Type.UNION;

    @Config
    public Matrix4 offset = new Matrix4();

    @Config
    public Function<Entity, Boolean> filter;

    private final Set<Entity> entities = new HashSet<>();
    private EntityManager entityManager;
    private Position position;

    @Override
    public void start(Scene scene, Entity entity) {
        entityManager = scene.getEntityManager();
        position = entity.getComponent(Position.class);
    }

    @Override
    protected void onEnter(Entity entity, OverlappedEntityInfo info) {
        if (filter == null || filter.apply(entity)) {
            entities.add(entity);
        }
    }

    @Override
    protected void onLeave(Entity entity, OverlappedEntityInfo info) {
        entities.remove(entity);
    }

    public void doCut() {
        System.gc();
        for (Entity entity1 : entities) {
            cut(entity1);
        }
        entities.clear();
        System.gc();
    }

    private void cut(Entity entity) {
        String id = entity.getId();

        try {
            entityManager.findEntityById(id);
            System.out.println("Cut:\tid=" + id);
        } catch (EntityManager.EntityManagerException e) {
            System.out.println("Already Removed:\tid=" + id);
            return;
        }

        Matrix4 tmpM = Matrix4Pool.obtain();
        Matrix4 transform = tmpM.set(position.getGlobalTransform()).mul(offset);
        List<Entity> newEntities = null;

        try {
            newEntities = BooleanUtil.cut(entity, cutter, transform, type);
        } catch (BooleanOperationException e) {
            e.printStackTrace();
        } finally {
            Matrix4Pool.free(tmpM);
        }

        if (newEntities == null) {
            return;
        }

        entityManager.removeEntity(id);
        for (Entity newEntity : newEntities) {
            entityManager.addEntity(newEntity);
        }
    }
}
