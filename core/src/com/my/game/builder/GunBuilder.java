package com.my.game.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.game.constraint.ConnectConstraint;
import com.my.game.constraint.HingeConstraint;
import com.my.game.script.GunController;
import com.my.game.script.GunScript;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityManager;
import com.my.utils.world.Prefab;
import com.my.utils.world.com.ConstraintController;
import com.my.utils.world.com.Position;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;

public class GunBuilder extends BaseBuilder {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();

        assetsManager.addAsset("barrel", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(mdBuilder.createBox(1, 1, 5, new Material(ColorAttribute.createDiffuse(Color.GREEN)), attributes)));
        assetsManager.addAsset("gunRotate", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(mdBuilder.createCylinder(1, 1, 1, 8, new Material(ColorAttribute.createDiffuse(Color.CYAN)), attributes)));

        assetsManager.addAsset("barrel", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btBoxShape(new Vector3(0.5f,0.5f,2.5f)), 5f));
        assetsManager.addAsset("gunRotate", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btCylinderShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
    }

    public GunBuilder(AssetsManager assetsManager, EntityManager entityManager) {
        super(assetsManager, entityManager);
    }

    public Entity createGun(String name, Entity base, Matrix4 transform) {

        // Gun Entity
        Entity entity = new Entity();
        entity.setName(name);
        entity.addComponent(new Position(new Matrix4()));
        GunScript gunScript = entity.addComponent(new GunScript());
        gunScript.bulletPrefab = assetsManager.getAsset("Bullet", Prefab.class);
        gunScript.bombPrefab = assetsManager.getAsset("Bomb", Prefab.class);
        entityManager.addEntity(entity);

        Entity rotate_Y = createRotate("rotate_Y", transform.cpy().translate(0, 0.5f, 0), new GunController(), base);
        Entity rotate_X = createRotate("rotate_X", transform.cpy().translate(0, 1.5f, 0).rotate(Vector3.Z, 90), new GunController(-90, 0), rotate_Y);
        Entity barrel = createBarrel("barrel", transform.cpy().translate(0, 1.5f, -3), rotate_X);

        rotate_Y.setParent(entity);
        rotate_X.setParent(entity);
        barrel.setParent(entity);

        return entity;
    }

    private Entity createBarrel(String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity("barrel");
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 2000));
        }
        return addEntity(name, transform, entity);
    }

    private Entity createRotate(String name, Matrix4 transform, ConstraintController controller, Entity base) {
        Matrix4 relTransform = (base == null) ? new Matrix4().inv().mul(transform) :
                new Matrix4(base.getComponent(Position.class).getLocalTransform()).inv().mul(transform);
        Entity entity = createEntity("gunRotate");
        if (base != null) {
            entity.addComponent(
                    new HingeConstraint(
                            base,
                            relTransform.rotate(Vector3.X, 90),
                            new Matrix4().rotate(Vector3.X, 90),
                            false
                    )
            );
        }
        entity.addComponent(controller);
        return addEntity(name, transform, entity);
    }
}
