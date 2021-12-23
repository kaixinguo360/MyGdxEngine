package com.my.game.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.game.constraint.ConnectConstraint;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityManager;
import com.my.utils.world.com.Position;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;
import com.my.utils.world.util.pool.Matrix4Pool;

public class SceneBuilder extends BaseBuilder {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ArrayMap<String, Model> models = new ArrayMap<>();
        ModelBuilder mdBuilder = new ModelBuilder();
        models.put("box", mdBuilder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.RED)), attributes));
        models.put("box1", mdBuilder.createBox(2, 1, 1, new Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY)), attributes));

        assetsManager.addAsset("box", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("box")));
        assetsManager.addAsset("box1", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("box1")));

        assetsManager.addAsset("box", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btBoxShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        assetsManager.addAsset("box1", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btBoxShape(new Vector3(1,0.5f,0.5f)), 50f));
    }

    public SceneBuilder(AssetsManager assetsManager, EntityManager entityManager) {
        super(assetsManager, entityManager);
    }

    public Entity createBox(String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity("box");
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 2000));
        }
        return addEntity(name, transform, entity);
    }

    public Entity createRunway(String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity("box");
        entity.setName(name);
        entityManager.addEntity(entity);
        Matrix4 tmpM = Matrix4Pool.obtain();
        for (int i = 0; i < 100; i++) {
            createBox("Box", tmpM.idt().translate(10, 0.5f, -10 * i).mulLeft(transform), base).setParent(entity);
            createBox("Box", tmpM.idt().translate(-10, 0.5f, -10 * i).mulLeft(transform), base).setParent(entity);
        }
        Matrix4Pool.free(tmpM);
        return entity;
    }

    public Entity createWall(String name, Matrix4 transform, int height) {
        Matrix4 tmpM = Matrix4Pool.obtain();
        Entity entity = new Entity();
        entity.setName(name);
        entity.addComponent(new Position(new Matrix4()));
        entityManager.addEntity(entity);
        for (int i = 0; i < height; i++) {
            float tmp = 0.5f + (i % 2);
            for (int j = 0; j < 10; j+=2) {
                Entity entity1 = createEntity("box1");
                entity1.setParent(entity);
                addEntity("Box", tmpM.setToTranslation(tmp + j, 0.5f + i, 0).mulLeft(transform), entity1);
            }
        }
        Matrix4Pool.free(tmpM);
        return entity;
    }

    public Entity createTower(String name, Matrix4 transform, int height) {
        Entity entity = new Entity();
        entity.setName(name);
        entity.addComponent(new Position(new Matrix4()));
        entityManager.addEntity(entity);
        createWall(name + "-1", transform.cpy(), height).setParent(entity);
        createWall(name + "-2", transform.cpy().set(transform).translate(0, 0, 10).rotate(Vector3.Y, 90), height).setParent(entity);
        createWall(name + "-3", transform.cpy().set(transform).translate(10, 0, 10).rotate(Vector3.Y, 180), height).setParent(entity);
        createWall(name + "-4", transform.cpy().set(transform).translate(10, 0, 0).rotate(Vector3.Y, 270), height).setParent(entity);
        return entity;
    }
}
