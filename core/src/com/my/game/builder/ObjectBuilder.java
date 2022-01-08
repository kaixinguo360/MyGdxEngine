package com.my.game.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.AssetsManager;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.gdx.Matrix4Pool;
import com.my.world.module.common.Position;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.model.Box;

public class ObjectBuilder extends BaseBuilder {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        assetsManager.addAsset("box", ModelRender.class, new Box(1, 1, 1, Color.RED, attributes));
        assetsManager.addAsset("box1", ModelRender.class, new Box(2, 1, 1, Color.LIGHT_GRAY, attributes));

        assetsManager.addAsset("box", TemplateRigidBody.class, new BoxBody(new Vector3(0.5f,0.5f,0.5f), 50f));
        assetsManager.addAsset("box1", TemplateRigidBody.class, new BoxBody(new Vector3(1,0.5f,0.5f), 50f));
    }

    public static String createRunway(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Runway");
        entity.addComponent(new Position(new Matrix4()));
        scene.getEntityManager().addEntity(entity);
        Matrix4 tmpM = Matrix4Pool.obtain();
        for (int i = 0; i < 100; i++) {
            createBox(scene, "Box", tmpM.idt().translate(10, 0.5f, -10 * i), null).setParent(entity);
            createBox(scene, "Box", tmpM.idt().translate(-10, 0.5f, -10 * i), null).setParent(entity);
        }
        Matrix4Pool.free(tmpM);
        return "Runway";
    }

    public static String createTower(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Tower");
        entity.addComponent(new Position(new Matrix4()));
        addEntity(scene, entity);
        createWall(scene, "Tower-1", new Matrix4()).setParent(entity);
        createWall(scene, "Tower-2", new Matrix4().translate(0, 0, 10).rotate(Vector3.Y, 90)).setParent(entity);
        createWall(scene, "Tower-3", new Matrix4().translate(10, 0, 10).rotate(Vector3.Y, 180)).setParent(entity);
        createWall(scene, "Tower-4", new Matrix4().translate(10, 0, 0).rotate(Vector3.Y, 270)).setParent(entity);
        return "Tower";
    }

    private static final int height = 5;
    public static Entity createWall(Scene scene, String name, Matrix4 transform) {
        Matrix4 tmpM = Matrix4Pool.obtain();
        Entity entity = new Entity();
        entity.setName(name);
        entity.addComponent(new Position(new Matrix4()));
        addEntity(scene, entity);
        for (int i = 0; i < height; i++) {
            float tmp = 0.5f + (i % 2);
            for (int j = 0; j < 10; j+=2) {
                Entity entity1 = createEntity(scene, "box1");
                entity1.setParent(entity);
                addEntity(scene, "Box", tmpM.setToTranslation(tmp + j, 0.5f + i, 0).mulLeft(transform), entity1);
            }
        }
        Matrix4Pool.free(tmpM);
        return entity;
    }

    public static Entity createBox(Scene scene, String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity(scene, "box");
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 2000));
        }
        return addEntity(scene, name, transform, entity);
    }
}
