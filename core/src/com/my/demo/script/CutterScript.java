package com.my.demo.script;

import com.badlogic.gdx.math.Matrix4;
import com.my.utils.bool.BooleanCutUtils;
import com.my.utils.bool.BooleanOperationException;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.EntityManager;
import com.my.world.core.Scene;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.physics.RigidBody;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.Render;
import com.my.world.module.script.ScriptSystem;

import java.util.List;

public class CutterScript implements ScriptSystem.OnStart, PhysicsSystem.OnCollision {

    @Config(type = Config.Type.Asset)
    public ModelRender cutter;

    @Config
    public BooleanCutUtils.Type type = BooleanCutUtils.Type.BOTH;

    @Config
    public Matrix4 offset = new Matrix4();

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
        if (entity == null || !entity.contains(Render.class) || !entity.contains(RigidBody.class)) return;
        String id = entity.getId();
        String name = entity.getName();

        if (!"Box".equals(name) && !"Brick".equals(name)) return;
        System.out.println("Cut:\tid=" + id);

        Matrix4 tmpM = Matrix4Pool.obtain();
        Matrix4 transform = tmpM.set(self.getComponent(Position.class).getGlobalTransform());
        transform.mul(offset);

        try {
            List<Entity> newEntities = BooleanCutUtils.cut(entity, cutter.model, transform, type);
            if (newEntities == null) {
                return;
            } else {
                try {
                    entityManager.removeEntity(id);
                } catch (EntityManager.EntityManagerException e) {
                    return;
                }
                for (Entity newEntity : newEntities) {
                    entityManager.getBatch().addEntity(newEntity);
                }
            }
        } catch (BooleanOperationException e) {
            entityManager.removeEntity(id);
            e.printStackTrace();
        }

        Matrix4Pool.free(tmpM);
    }
}
