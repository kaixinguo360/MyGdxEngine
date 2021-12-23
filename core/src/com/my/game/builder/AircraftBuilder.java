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
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.game.constraint.ConnectConstraint;
import com.my.game.constraint.HingeConstraint;
import com.my.game.script.AircraftBombCollisionHandler;
import com.my.game.script.AircraftController;
import com.my.game.script.AircraftScript;
import com.my.game.script.RemoveScript;
import com.my.game.script.motion.Lift;
import com.my.game.script.motion.LimitedForce;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityManager;
import com.my.utils.world.Prefab;
import com.my.utils.world.com.Collision;
import com.my.utils.world.com.ConstraintController;
import com.my.utils.world.com.Position;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;

public class AircraftBuilder extends BaseBuilder {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ArrayMap<String, Model> models = new ArrayMap<>();
        ModelBuilder mdBuilder = new ModelBuilder();

        models.put("bomb", mdBuilder.createCapsule(0.5f, 2, 8, new Material(ColorAttribute.createDiffuse(Color.GRAY)), attributes));
        models.put("body", mdBuilder.createBox(1, 1, 5, new Material(ColorAttribute.createDiffuse(Color.GREEN)), attributes));
        models.put("wing", mdBuilder.createBox(2, 0.2f, 1, new Material(ColorAttribute.createDiffuse(Color.BLUE)), attributes));
        models.put("rotate", mdBuilder.createCylinder(1, 1, 1, 8, new Material(ColorAttribute.createDiffuse(Color.CYAN)), attributes));
        models.put("engine", mdBuilder.createCone(0.9f, 1, 0.9f, 18, new Material(ColorAttribute.createDiffuse(Color.YELLOW)), attributes));

        assetsManager.addAsset("bomb", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("bomb")));
        assetsManager.addAsset("body", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("body")));
        assetsManager.addAsset("wing", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("wing")));
        assetsManager.addAsset("rotate", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("rotate")));
        assetsManager.addAsset("engine", RenderSystem.RenderModel.class, new RenderSystem.RenderModel(models.get("engine")));

        assetsManager.addAsset("bomb", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btCapsuleShape(0.5f, 1), 50f));
        assetsManager.addAsset("body", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btBoxShape(new Vector3(0.5f,0.5f,2.5f)), 50f));
        assetsManager.addAsset("wing", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btBoxShape(new Vector3(1f,0.1f,0.5f)), 25f));
        assetsManager.addAsset("rotate", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btCylinderShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        assetsManager.addAsset("engine", btRigidBody.btRigidBodyConstructionInfo.class, PhysicsSystem.getRigidBodyConfig(new btConeShape(0.45f,1), 50));
    }

    public final static short BOMB_FLAG = 1 << 8;
    public final static short AIRCRAFT_FLAG = 1 << 9;
    public final static short ALL_FLAG = -1;

    public AircraftBuilder(AssetsManager assetsManager, EntityManager entityManager) {
        super(assetsManager, entityManager);
    }

    public Entity createBomb(String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity("bomb");
        entity.addComponent(new Collision(BOMB_FLAG, ALL_FLAG));
        entity.addComponent(new RemoveScript());
        entity.addComponent(new AircraftBombCollisionHandler());
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 2000));
        }
        return addEntity(name, transform, entity);
    }

    public Entity createAircraft(String name, Matrix4 transform, float force, float maxVelocity) {

        // Aircraft Entity
        Entity entity = new Entity();
        entity.setName(name);
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new AircraftScript()).bombPrefab = assetsManager.getAsset("Bomb", Prefab.class);
        entityManager.addEntity(entity);

        // Body
        Entity body = createBody("body", transform.cpy().translate(0, 0.5f, -3), null);
        Entity engine = createEngine("engine", transform.cpy().translate(0, 0.6f, -6).rotate(Vector3.X, -90), force, maxVelocity, body);

        // Left
        Entity rotate_L = createRotate("rotate_L", transform.cpy().translate(-1, 0.5f, -5).rotate(Vector3.Z, 90), new AircraftController(-0.15f, 0.2f, 0.5f), body);
        Entity wing_L1 = createWing("wing_L1", transform.cpy().translate(-2.5f, 0.5f, -5).rotate(Vector3.X, 14), rotate_L);
        Entity wing_L2 = createWing("wing_L2", transform.cpy().translate(-4.5f, 0.5f, -5).rotate(Vector3.X, 14), wing_L1);

        // Right
        Entity rotate_R = createRotate("rotate_R", transform.cpy().translate(1, 0.5f, -5).rotate(Vector3.Z, 90), new AircraftController(-0.15f, 0.2f, 0.5f), body);
        Entity wing_R1 = createWing("wing_R1", transform.cpy().translate(2.5f, 0.5f, -5).rotate(Vector3.X, 14), rotate_R);
        Entity wing_R2 = createWing("wing_R2", transform.cpy().translate(4.5f, 0.5f, -5).rotate(Vector3.X, 14), wing_R1);

        // Horizontal Tail
        Entity rotate_T = createRotate("rotate_T", transform.cpy().translate(0, 0.5f, 0.1f).rotate(Vector3.Z, 90), new AircraftController(-0.2f, 0.2f, 1f), body);
        Entity wing_TL = createWing("wing_TL", transform.cpy().translate(-1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), rotate_T);
        Entity wing_TR = createWing("wing_TR", transform.cpy().translate(1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), rotate_T);

        // Vertical Tail
        Entity wing_VL = createWing("wing_VL", transform.cpy().translate(-0.6f, 1f, -1).rotate(Vector3.Z, 90), body);
        Entity wing_VR = createWing("wing_VR", transform.cpy().translate(0.6f, 1f, -1).rotate(Vector3.Z, 90), body);

        body.setParent(entity);
        engine.setParent(entity);
        rotate_L.setParent(entity);
        wing_L1.setParent(entity);
        wing_L2.setParent(entity);
        rotate_R.setParent(entity);
        wing_R1.setParent(entity);
        wing_R2.setParent(entity);
        rotate_T.setParent(entity);
        wing_TL.setParent(entity);
        wing_TR.setParent(entity);
        wing_VL.setParent(entity);
        wing_VR.setParent(entity);

        return entity;
    }

    private Entity createBody(String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity("body");
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 2000));
        }
        return addEntity(name, transform, entity);
    }

    private Entity createWing(String name, Matrix4 transform, Entity base) {
        Entity entity = createEntity("wing");
        entity.addComponent(new Lift(new Vector3(0, 200, 0)));
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 500));
        }
        return addEntity(name, transform, entity);
    }

    private Entity createRotate(String name, Matrix4 transform, ConstraintController controller, Entity base) {
        Matrix4 relTransform = new Matrix4(base.getComponent(Position.class).getLocalTransform()).inv().mul(transform);
        Entity entity = createEntity("rotate");
        entity.addComponent(controller);
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
        return addEntity(name, transform, entity);
    }

    private Entity createEngine(String name, Matrix4 transform, float force, float maxVelocity, Entity base) {
        Entity entity = createEntity("engine");
        entity.addComponent(new LimitedForce(maxVelocity, new Vector3(0, force, 0), new Vector3()));
        if (base != null) {
            entity.addComponent(new ConnectConstraint(base, 2000));
        }
        return addEntity(name, transform, entity);
    }
}
