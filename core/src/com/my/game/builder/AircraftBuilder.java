package com.my.game.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.game.script.AircraftController;
import com.my.game.script.AircraftScript;
import com.my.world.core.AssetsManager;
import com.my.world.core.Entity;
import com.my.world.core.EntityManager;
import com.my.world.core.Prefab;
import com.my.world.module.common.Position;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.constraint.ConnectConstraint;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.physics.motion.Lift;
import com.my.world.module.physics.motion.LimitedForce;
import com.my.world.module.physics.rigidbody.BoxBody;
import com.my.world.module.physics.rigidbody.ConeBody;
import com.my.world.module.physics.rigidbody.CylinderBody;
import com.my.world.module.physics.script.ConstraintController;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.model.Box;
import com.my.world.module.render.model.Cone;
import com.my.world.module.render.model.Cylinder;

public class AircraftBuilder extends BaseBuilder {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

        assetsManager.addAsset("body", ModelRender.class, new Box(1, 1, 5, Color.GREEN, attributes));
        assetsManager.addAsset("wing", ModelRender.class, new Box(2, 0.2f, 1, Color.BLUE, attributes));
        assetsManager.addAsset("rotate", ModelRender.class, new Cylinder(1, 1, 1, 8, Color.CYAN, attributes));
        assetsManager.addAsset("engine", ModelRender.class, new Cone(0.9f, 1, 0.9f, 18, Color.YELLOW, attributes));

        assetsManager.addAsset("body", TemplateRigidBody.class, new BoxBody(new Vector3(0.5f,0.5f,2.5f), 50f));
        assetsManager.addAsset("wing", TemplateRigidBody.class, new BoxBody(new Vector3(1f,0.1f,0.5f), 25f));
        assetsManager.addAsset("rotate", TemplateRigidBody.class, new CylinderBody(new Vector3(0.5f,0.5f,0.5f), 50f));
        assetsManager.addAsset("engine", TemplateRigidBody.class, new ConeBody(0.45f,1, 50));
    }

    public AircraftBuilder(AssetsManager assetsManager, EntityManager entityManager) {
        super(assetsManager, entityManager);
    }

    public Entity createAircraft(String name, Matrix4 transform, float force, float maxVelocity) {

        // Aircraft Entity
        Entity entity = new Entity();
        entity.setName(name);
        entity.addComponent(new Position(new Matrix4()));
        AircraftScript aircraftScript = entity.addComponent(new AircraftScript());
        aircraftScript.bulletPrefab = assetsManager.getAsset("Bullet", Prefab.class);
        aircraftScript.bombPrefab = assetsManager.getAsset("Bomb", Prefab.class);
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
