package com.my.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.Constraint;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Serialization;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;

public class SceneBuilder {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ArrayMap<String, Model> models = new ArrayMap<>();
        ModelBuilder mdBuilder = new ModelBuilder();
        models.put("box", mdBuilder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.RED)), attributes));
        models.put("box1", mdBuilder.createBox(2, 1, 1, new Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY)), attributes));

        assetsManager.addAsset("box", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("box")));
        assetsManager.addAsset("box1", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("box1")));

        assetsManager.addAsset("box", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btBoxShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        assetsManager.addAsset("box1", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btBoxShape(new Vector3(1,0.5f,0.5f)), 50f));
    }

    // ----- Temporary ----- //
    private final Matrix4 tmpM = new Matrix4();

    // ----- Variables ----- //
    private final World world;
    private final AssetsManager assetsManager;

    public SceneBuilder(World world) {
        this.world = world;
        this.assetsManager = world.getAssetsManager();

        Serialization.Serializer serializer = new Serializer(world);
        Serialization.addSerializer("box", serializer);
    }

    // ----- Builder Methods ----- //

    private int boxNum = 0;
    public Entity createBox(Matrix4 transform, String base) {
        Entity entity = new MyInstance(assetsManager, "box", "box");
        String id = "Box-" + boxNum++;
        addObject(
                id, transform, entity,
                base == null ? null : new Constraints.ConnectConstraint(base, id, null, 2000)
        );
        return entity;
    }

    public void createWall(Matrix4 transform, int height) {
        for (int i = 0; i < height; i++) {
            float tmp = 0.5f + (i % 2);
            for (int j = 0; j < 10; j+=2) {
                addObject(
                        "Box-" + boxNum++,
                        tmpM.setToTranslation(tmp + j, 0.5f + i, 0).mulLeft(transform),
                        new MyInstance(assetsManager, "box1", "box1"), null
                );
            }
        }
    }

    public void createTower(Matrix4 transform, int height) {
        createWall(transform.cpy(), height);
        createWall(transform.cpy().set(transform).translate(0, 0, 10).rotate(Vector3.Y, 90), height);
        createWall(transform.cpy().set(transform).translate(10, 0, 10).rotate(Vector3.Y, 180), height);
        createWall(transform.cpy().set(transform).translate(10, 0, 0).rotate(Vector3.Y, 270), height);
    }

    // ----- Private ----- //
    private String addObject(String id, Matrix4 transform, Entity entity, Constraint constraint) {
        entity.setId(id);
        world.getEntityManager().addEntity(entity)
                .getComponent(Position.class).transform.set(transform);
        if (constraint != null) {
            entity.addComponent(constraint);
        }
        return id;
    }
}
