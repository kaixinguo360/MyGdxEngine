package com.my.world.enhanced.bool.entity;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.EntityManager;
import com.my.world.core.Scene;
import com.my.world.enhanced.bool.operation.BooleanOperationException;
import com.my.world.enhanced.bool.util.BooleanEntityUtils;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.Render;
import com.my.world.module.script.ScriptSystem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CutterScript implements ScriptSystem.OnStart, PhysicsSystem.OnCollision, ScriptSystem.OnRemoved {

    @Config(type = Config.Type.Asset)
    public ModelRender cutter;

    @Config
    public BooleanEntityUtils.Type type = BooleanEntityUtils.Type.BOTH;

    @Config
    public Matrix4 offset = new Matrix4();

    private final Set<Entity> entities = new HashSet<>();
    private EntityManager entityManager;
    private Entity self;

    @Override
    public void start(Scene scene, Entity entity) {
        this.entityManager = scene.getEntityManager();
        this.self = entity;
        entityManager.getBatch().removeEntity(self.getId());
    }

    @Override
    public void collision(Entity entity) {
        if (entity.contains(Render.class) && entity.contains(RigidBody.class)
                && ("Box".equals(entity.getName()) || "Brick".equals(entity.getName()))) {
            System.out.println("Pick:\tid=" + entity.getId());
            entities.add(entity);
        }
    }

    @Override
    public void removed(Scene scene, Entity entity) {
        for (Entity entity1 : entities) {
            cut(entity1);
        }
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
        Matrix4 transform = tmpM.set(self.getComponent(Position.class).getGlobalTransform()).mul(offset);
        List<Entity> newEntities = null;

        try {
            newEntities = BooleanEntityUtils.cut(entity, cutter.model, transform, type);
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
