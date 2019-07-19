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
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.game.MyInstance;
import com.my.game.constraint.ConnectConstraint;
import com.my.game.constraint.HingeConstraint;
import com.my.game.script.GunBulletCollisionHandler;
import com.my.game.script.GunController;
import com.my.game.script.GunScript;
import com.my.game.script.RemoveScript;
import com.my.utils.world.*;
import com.my.utils.world.com.Collision;
import com.my.utils.world.com.Constraint;
import com.my.utils.world.com.ConstraintController;
import com.my.utils.world.com.Position;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;

public class GunBuilder {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();
        ArrayMap<String, Model> models = new ArrayMap<>();

        models.put("bullet", mdBuilder.createCapsule(0.5f, 2, 8, new Material(ColorAttribute.createDiffuse(Color.YELLOW)), VertexAttributes.Usage.Position));
        models.put("barrel", mdBuilder.createBox(1, 1, 5, new Material(ColorAttribute.createDiffuse(Color.GREEN)), attributes));
        models.put("gunRotate", mdBuilder.createCylinder(1, 1, 1, 8, new Material(ColorAttribute.createDiffuse(Color.CYAN)), attributes));

        assetsManager.addAsset("bullet", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("bullet")));
        assetsManager.addAsset("barrel", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("barrel")));
        assetsManager.addAsset("gunRotate", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("gunRotate")));

        assetsManager.addAsset("bullet", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btCapsuleShape(0.5f, 1), 50f));
        assetsManager.addAsset("barrel", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btBoxShape(new Vector3(0.5f,0.5f,2.5f)), 5f));
        assetsManager.addAsset("gunRotate", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btCylinderShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
    }

    // ----- Constants ----- //

    public final static short BOMB_FLAG = 1 << 8;
    public final static short GUN_FLAG = 1 << 9;
    public final static short ALL_FLAG = -1;

    // ----- Variables ----- //

    private final AssetsManager assetsManager;
    private final EntityManager entityManager;

    public GunBuilder(World world) {
        this.assetsManager = world.getAssetsManager();
        this.entityManager = world.getEntityManager();
    }

    public GunBuilder(AssetsManager assetsManager, EntityManager entityManager) {
        this.assetsManager = assetsManager;
        this.entityManager = entityManager;
    }

    // ----- Builder Methods ----- //

    public Entity createBullet(String name, Matrix4 transform, Entity base) {
        MyInstance bullet = new MyInstance(assetsManager, "bullet", null, new Collision(BOMB_FLAG, ALL_FLAG));
        addObject(
                name, transform, bullet,
                base == null ? null : new ConnectConstraint(base, 2000)
        );
        bullet.addComponent(new RemoveScript());
        bullet.addComponent(new GunBulletCollisionHandler());
        return bullet;
    }

    private Entity createBarrel(String name, Matrix4 transform, Entity base) {
        return addObject(
                name, transform, new MyInstance(assetsManager, "barrel"),
                base == null ? null : new ConnectConstraint(base, 2000)
        );
    }

    private Entity createRotate(String name, Matrix4 transform, ConstraintController controller, Entity base) {
        Matrix4 relTransform = (base == null) ? new Matrix4().inv().mul(transform) :
                new Matrix4(base.getComponent(Position.class).getLocalTransform()).inv().mul(transform);
        Entity entity = addObject(
                name, transform, new MyInstance(assetsManager, "gunRotate"),
                base == null ? null : new HingeConstraint(
                        base,
                        relTransform.rotate(Vector3.X, 90),
                        new Matrix4().rotate(Vector3.X, 90),
                        false)
        );
        entity.addComponent(controller);
        return entity;
    }

    public Entity createGun(String name, Entity base, Matrix4 transform) {

        // Gun Entity
        Entity entity = new Entity();
        entity.setName(name);
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GunScript()).bulletPrefab = assetsManager.getAsset("Bullet", Prefab.class);
        entityManager.addEntity(entity);

        Entity rotate_Y = createRotate("rotate_Y", transform.cpy().translate(0, 0.5f, 0), new GunController(), base);
        Entity rotate_X = createRotate("rotate_X", transform.cpy().translate(0, 1.5f, 0).rotate(Vector3.Z, 90), new GunController(-90, 0), rotate_Y);
        Entity barrel = createBarrel("barrel", transform.cpy().translate(0, 1.5f, -3), rotate_X);

        rotate_Y.setParent(entity);
        rotate_X.setParent(entity);
        barrel.setParent(entity);

        return entity;
    }

    // ----- Private ----- //

    private Entity addObject(String name, Matrix4 transform, Entity entity, Constraint constraint) {
        entity.setName(name);
        entity.getComponent(Position.class).setLocalTransform(transform);
        if (constraint != null) entity.addComponent(constraint);
        entityManager.addEntity(entity);
        return entity;
    }
}
