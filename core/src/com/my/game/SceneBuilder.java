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
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Serialization;
import com.my.utils.world.sys.ConstraintSystem;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;

public class SceneBuilder {

    // ----- Temporary ----- //
    private static final Matrix4 tmpM = new Matrix4();

    // ----- Constants ----- //
    private final static short BOMB_FLAG = 1 << 8;
    private final static short ALL_FLAG = -1;

    // ----- Variables ----- //
    private static World world;
    private static AssetsManager assetsManager;
    private static ConstraintSystem constraintSystem;

    // ----- Init ----- //
    private static ArrayMap<String, Model> models = new ArrayMap<>();
    public static void init(World world) {
        SceneBuilder.world = world;
        SceneBuilder.assetsManager = world.getAssetsManager();
        SceneBuilder.constraintSystem = world.getSystemManager().getSystem(ConstraintSystem.class);

        initAssets(assetsManager);

        Serialization.Serializer serializer = new Serializer(world);
        Serialization.addSerializer("box", serializer);
    }

    public static void initAssets(AssetsManager assetsManager) {

        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();
        models.put("box", mdBuilder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.RED)), attributes));
        models.put("box1", mdBuilder.createBox(2, 1, 1, new Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY)), attributes));

        assetsManager.addAsset("box", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("box")));
        assetsManager.addAsset("box1", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("box1")));

        assetsManager.addAsset("box", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btBoxShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        assetsManager.addAsset("box1", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btBoxShape(new Vector3(1,0.5f,0.5f)), 50f));
    }

    // ----- Builder Methods ----- //
    private static int boxNum = 0;
    public static Entity createBox(Matrix4 transform, String base) {
        Entity entity = new MyInstance("box", "box");
        addObject(
                "Box-" + boxNum++,
                transform,
                entity,
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
        return entity;
    }

    public static void createWall(Matrix4 transform, int height) {
        for (int i = 0; i < height; i++) {
            float tmp = 0.5f + (i % 2);
            for (int j = 0; j < 10; j+=2) {
                addObject(
                        "Box-" + boxNum++,
                        tmpM.setToTranslation(tmp + j, 0.5f + i, 0).mulLeft(transform),
                        new MyInstance("box1", "box1"), null, null
                );
            }
        }
    }

    public static void createTower(Matrix4 transform, int height) {
        createWall(transform.cpy(), height);
        createWall(transform.cpy().set(transform).translate(0, 0, 10).rotate(Vector3.Y, 90), height);
        createWall(transform.cpy().set(transform).translate(10, 0, 10).rotate(Vector3.Y, 180), height);
        createWall(transform.cpy().set(transform).translate(10, 0, 0).rotate(Vector3.Y, 270), height);
    }

    // ----- Private ----- //
    private static String addObject(String id, Matrix4 transform, Entity entity, String base, ConstraintSystem.Config constraint) {
        entity.setId(id);
        world.getEntityManager().addEntity(entity)
                .getComponent(Position.class).transform.set(transform);
        if (base != null) constraintSystem.addConstraint(base, id, constraint);
        return id;
    }
}
