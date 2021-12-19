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
import com.my.game.MyInstance;
import com.my.game.constraint.ConnectConstraint;
import com.my.game.constraint.HingeConstraint;
import com.my.game.script.AircraftController;
import com.my.game.script.AircraftScript;
import com.my.game.script.motion.Lift;
import com.my.game.script.motion.LimitedForce;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.Constraint;
import com.my.utils.world.com.ConstraintController;
import com.my.utils.world.com.Position;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;

public class AircraftBuilder {

    // ----- Constants ----- //
    private static final String group = "group";

    // ----- Variables ----- //
    private final World world;
    private final AssetsManager assetsManager;

    public AircraftBuilder(World world) {
        this.world = world;
        this.assetsManager = world.getAssetsManager();
    }

    // ----- Builder Methods ----- //

    private int bodyNum = 0;

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

    private Entity createBody(Matrix4 transform, Entity base) {
        String id = "Body-" + bodyNum++;
        return addObject(
                id, transform, new MyInstance(assetsManager, "body"),
                base == null ? null : new ConnectConstraint(base.getId(), id, 2000)
        );
    }

    private int wingNum = 0;

    private Entity createWing(Matrix4 transform, Entity base) {
        String id = "Wing-" + wingNum++;
        return addObject(
                id, transform, new MyInstance(assetsManager, "wing", new Lift(new Vector3(0, 200, 0))),
                base == null ? null : new ConnectConstraint(base.getId(), id, 500)
        );
    }

    private int rotateNum = 0;

    private Entity createRotate(Matrix4 transform, ConstraintController controller, Entity base) {
        Matrix4 relTransform = new Matrix4(base.getComponent(Position.class).transform).inv().mul(transform);
        String id = "Rotate-" + rotateNum++;
        Entity entity = addObject(
                id, transform, new MyInstance(assetsManager, "rotate"),
                base == null ? null : new HingeConstraint(
                        base.getId(), id,
                        relTransform.rotate(Vector3.X, 90),
                        new Matrix4().rotate(Vector3.X, 90),
                        false)
        );
        entity.addComponent(controller);
        return entity;
    }

    private int engineNum = 0;

    private Entity createEngine(Matrix4 transform, float force, float maxVelocity, Entity base) {
        String id = "Engine-" + engineNum++;
        return addObject(
                id, transform,
                new MyInstance(assetsManager, "engine", new LimitedForce(maxVelocity, new Vector3(0, force, 0), new Vector3())),
                base == null ? null : new ConnectConstraint(base.getId(), id, 2000)
        );
    }

    private int aircraftNum = 0;

    public Entity createAircraft(Matrix4 transform, float force, float maxVelocity) {

        // Aircraft
        AircraftScript aircraftScript = new AircraftScript();

        // Body
        aircraftScript.body = createBody(transform.cpy().translate(0, 0.5f, -3), null);
        aircraftScript.engine = createEngine(transform.cpy().translate(0, 0.6f, -6).rotate(Vector3.X, -90), force, maxVelocity, aircraftScript.body);

        // Left
        aircraftScript.aircraftController_L = new AircraftController(-0.15f, 0.2f, 0.5f);
        aircraftScript.rotate_L = createRotate(transform.cpy().translate(-1, 0.5f, -5).rotate(Vector3.Z, 90), aircraftScript.aircraftController_L, aircraftScript.body);
        aircraftScript.wing_L1 = createWing(transform.cpy().translate(-2.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraftScript.rotate_L);
        aircraftScript.wing_L2 = createWing(transform.cpy().translate(-4.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraftScript.wing_L1);

        // Right
        aircraftScript.aircraftController_R = new AircraftController(-0.15f, 0.2f, 0.5f);
        aircraftScript.rotate_R = createRotate(transform.cpy().translate(1, 0.5f, -5).rotate(Vector3.Z, 90), aircraftScript.aircraftController_R, aircraftScript.body);
        aircraftScript.wing_R1 = createWing(transform.cpy().translate(2.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraftScript.rotate_R);
        aircraftScript.wing_R2 = createWing(transform.cpy().translate(4.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraftScript.wing_R1);

        // Horizontal Tail
        aircraftScript.aircraftController_T = new AircraftController(-0.2f, 0.2f, 1f);
        aircraftScript.rotate_T = createRotate(transform.cpy().translate(0, 0.5f, 0.1f).rotate(Vector3.Z, 90), aircraftScript.aircraftController_T, aircraftScript.body);
        aircraftScript.wing_TL = createWing(transform.cpy().translate(-1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), aircraftScript.rotate_T);
        aircraftScript.wing_TR = createWing(transform.cpy().translate(1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), aircraftScript.rotate_T);

        // Vertical Tail
        aircraftScript.wing_VL = createWing(transform.cpy().translate(-0.6f, 1f, -1).rotate(Vector3.Z, 90), aircraftScript.body);
        aircraftScript.wing_VR = createWing(transform.cpy().translate(0.6f, 1f, -1).rotate(Vector3.Z, 90), aircraftScript.body);

        // Aircraft Entity
        Entity entity = new Entity();
        entity.setId("Aircraft-" + aircraftNum++);
        entity.addComponent(aircraftScript);
        world.getEntityManager().addEntity(entity);

        return entity;
    }

    // ----- Private ----- //
    private Entity addObject(String id, Matrix4 transform, Entity entity, Constraint constraint) {
        entity.setId(id);
        world.getEntityManager().addEntity(entity)
                .getComponent(Position.class).transform.set(transform);
        if (constraint != null) {
            entity.addComponent(constraint);
        }
        return entity;
    }
}
