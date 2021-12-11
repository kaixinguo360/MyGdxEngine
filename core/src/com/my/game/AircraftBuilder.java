package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.dynamics.btHingeConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.Collision;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.com.ScriptComponent;
import com.my.utils.world.sys.ConstraintSystem;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;
import com.my.utils.world.sys.ScriptSystem;

public class AircraftBuilder {

    // ----- Temporary ----- //
    private static final Vector3 tmpV1 = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();
    private static final Quaternion tmpQ = new Quaternion();
    private static final String group = "group";

    // ----- Constants ----- //
    private final static short BOMB_FLAG = 1 << 8;
    private final static short AIRCRAFT_FLAG = 1 << 9;
    private final static short ALL_FLAG = -1;

    // ----- Variables ----- //
    private static World world;
    private static AssetsManager assetsManager;
    private static PhysicsSystem physicsSystem;
    private static ConstraintSystem constraintSystem;

    // ----- Init ----- //
    public static void init(World world) {
        AircraftBuilder.world = world;
        AircraftBuilder.assetsManager = world.getAssetsManager();
        AircraftBuilder.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
        AircraftBuilder.constraintSystem = world.getSystemManager().getSystem(ConstraintSystem.class);

        initAssets(assetsManager);
    }

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ArrayMap<String, Model> models = new ArrayMap<>();
        ModelBuilder mdBuilder = new ModelBuilder();

        models.put("bomb", mdBuilder.createCapsule(0.5f, 2, 8, new Material(ColorAttribute.createDiffuse(Color.GRAY)), attributes));
        models.put("body", mdBuilder.createBox(1, 1, 5, new Material(ColorAttribute.createDiffuse(Color.GREEN)), attributes));
        models.put("wing", mdBuilder.createBox(2, 0.2f, 1, new Material(ColorAttribute.createDiffuse(Color.BLUE)), attributes));
        models.put("rotate", mdBuilder.createCylinder(1, 1, 1, 8, new Material(ColorAttribute.createDiffuse(Color.CYAN)), attributes));
        models.put("engine", mdBuilder.createCone(0.9f, 1, 0.9f, 18, new Material(ColorAttribute.createDiffuse(Color.YELLOW)), attributes));

        assetsManager.addAsset("bomb", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("bomb")));
        assetsManager.addAsset("body", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("body")));
        assetsManager.addAsset("wing", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("wing")));
        assetsManager.addAsset("rotate", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("rotate")));
        assetsManager.addAsset("engine", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("engine")));

        assetsManager.addAsset("bomb", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btCapsuleShape(0.5f, 1), 50f));
        assetsManager.addAsset("body", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btBoxShape(new Vector3(0.5f,0.5f,2.5f)), 50f));
        assetsManager.addAsset("wing", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btBoxShape(new Vector3(1f,0.1f,0.5f)), 25f));
        assetsManager.addAsset("rotate", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btCylinderShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        assetsManager.addAsset("engine", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btConeShape(0.45f,1), 50));
    }

    // ----- Builder Methods ----- //
    private static int bombNum = 0;
    private static Entity createBomb(Matrix4 transform, Entity base) {
        Entity entity = new MyInstance("bomb", "bomb", null,
                new Collision(BOMB_FLAG, ALL_FLAG, assetsManager.getAsset("BombCollisionHandler", PhysicsSystem.CollisionHandler.class)));
        addObject(
                "Bomb-" + bombNum++,
                transform,
                entity,
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
        ScriptComponent scriptComponent = new ScriptComponent();
        scriptComponent.script = assetsManager.getAsset("RemoveScript", ScriptSystem.Script.class);
        entity.addComponent(scriptComponent);
        return entity;
    }

    private static int bodyNum = 0;
    private static Entity createBody(Matrix4 transform, Entity base) {
        return addObject(
                "Body-" + bodyNum++,
                transform,
                new MyInstance("body", group),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
    }

    private static int wingNum = 0;
    private static Entity createWing(Matrix4 transform, Entity base) {
        return addObject(
                "Wing-" + wingNum++,
                transform,
                new MyInstance("wing", group, Motions.Lift.getConfig(assetsManager, new Vector3(0, 200, 0))),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint(500)
        );
    }

    private static int rotate = 0;
    private static Entity createRotate(Matrix4 transform, ConstraintSystem.Controller controller, Entity base) {
        Matrix4 relTransform = new Matrix4(base.getComponent(Position.class).transform).inv().mul(transform);
        Entity entity = addObject(
                "Rotate-" + rotate++,
                transform,
                new MyInstance("rotate", group),
                base,
                base == null ? null : new ConstraintSystem.HingeConstraint(
                        relTransform.rotate(Vector3.X, 90),
                        new Matrix4().rotate(Vector3.X, 90),
                        false)
        );
        constraintSystem.addController(entity.getId(), base.getId(), controller);
        return entity;
    }

    private static int engineNum = 0;
    private static Entity createEngine(Matrix4 transform, float force, float maxVelocity, Entity base) {
        return addObject(
                "Engine-" + engineNum++,
                transform,
                new MyInstance("engine", group, Motions.LimitedForce.getConfig(assetsManager, maxVelocity, new Vector3(0, force, 0), new Vector3())),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
    }

    public static Aircraft createAircraft(Matrix4 transform, float force, float maxVelocity) {
        return new Aircraft(transform, force, maxVelocity);
    }

    public static class Aircraft implements Controllable {

        private Entity body;
        private Entity engine;
        private Entity rotate_L, rotate_R, rotate_T;
        private Entity wing_L1, wing_L2;
        private Entity wing_R1, wing_R2;
        private Entity wing_TL, wing_TR;
        private Entity wing_VL, wing_VR;
        private Controller controller_L = new Controller(-0.15f, 0.2f, 0.5f);
        private Controller controller_R = new Controller(-0.15f, 0.2f, 0.5f);
        private Controller controller_T = new Controller(-0.2f, 0.2f, 1f);

        private Aircraft(Matrix4 transform, float force, float maxVelocity) {

            // Body
            body = createBody(transform.cpy().translate(0, 0.5f, -3), null);
            engine = createEngine(transform.cpy().translate(0, 0.6f, -6).rotate(Vector3.X, -90), force, maxVelocity, body);

            // Left
            rotate_L = createRotate(transform.cpy().translate(-1, 0.5f, -5).rotate(Vector3.Z, 90),
                    controller_L, body);
            wing_L1 = createWing(transform.cpy().translate(-2.5f, 0.5f, -5).rotate(Vector3.X, 14), rotate_L);
            wing_L2 = createWing(transform.cpy().translate(-4.5f, 0.5f, -5).rotate(Vector3.X, 14), wing_L1);

            // Right
            rotate_R = createRotate(transform.cpy().translate(1, 0.5f, -5).rotate(Vector3.Z, 90),
                    controller_R, body);
            wing_R1 = createWing(transform.cpy().translate(2.5f, 0.5f, -5).rotate(Vector3.X, 14), rotate_R);
            wing_R2 = createWing(transform.cpy().translate(4.5f, 0.5f, -5).rotate(Vector3.X, 14), wing_R1);

            // Horizontal Tail
            rotate_T = createRotate(transform.cpy().translate(0, 0.5f, 0.1f).rotate(Vector3.Z, 90),
                    controller_T, body);
            wing_TL = createWing(transform.cpy().translate(-1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), rotate_T);
            wing_TR = createWing(transform.cpy().translate(1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), rotate_T);

            // Vertical Tail
            wing_VL = createWing(transform.cpy().translate(-0.6f, 1f, -1).rotate(Vector3.Z, 90), body);
            wing_VR = createWing(transform.cpy().translate(0.6f, 1f, -1).rotate(Vector3.Z, 90), body);
        }

        public void update() {
            float v1 = 1f;
            float v2 = 0.5f;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) controller_T.rotate(v1);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) controller_T.rotate(-v1);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                controller_L.rotate(v2);
                controller_R.rotate(-v2);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                controller_L.rotate(-v2);
                controller_R.rotate(v2);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.J)) fire();
        }
        public void fire() {
            tmpM.set(getTransform()).translate(0, 0, -20 + (float) (Math.random() * 15)).rotate(Vector3.X, 90);
            getTransform().getRotation(tmpQ);
            tmpV1.set(getBody().getLinearVelocity());
            tmpV1.add(new Vector3(0, 0, -1).mul(tmpQ).scl(2000));
            btRigidBody body = createBomb(tmpM, null).getComponent(RigidBody.class).body;
            body.setLinearVelocity(tmpV1);
            body.setCcdMotionThreshold(1e-7f);
            body.setCcdSweptSphereRadius(2);
        }
        public void explode() {
            System.out.println("Explosion!");
            constraintSystem.remove(world, body.getId());
            constraintSystem.remove(world, engine.getId());
            constraintSystem.remove(world, rotate_L.getId());
            constraintSystem.remove(world, rotate_R.getId());
            constraintSystem.remove(world, rotate_T.getId());
            constraintSystem.remove(world, wing_L1.getId());
            constraintSystem.remove(world, wing_L2.getId());
            constraintSystem.remove(world, wing_R1.getId());
            constraintSystem.remove(world, wing_R2.getId());
            constraintSystem.remove(world, wing_TL.getId());
            constraintSystem.remove(world, wing_TR.getId());
            constraintSystem.remove(world, wing_VL.getId());
            constraintSystem.remove(world, wing_VR.getId());
            physicsSystem.addExplosion(getTransform().getTranslation(tmpV1), 2000);
        }
        public void setCamera(PerspectiveCamera camera, int index) {
            if (index == 0) {
                Matrix4 transform = getTransform();
                camera.position.set(0, 0.8f, -1.5f).mul(transform);
                camera.direction.set(0, 0, -1).rot(transform);
                camera.up.set(0, 1 , 0).rot(transform);
                camera.update();
            } else if (index == 1){
                Matrix4 transform = getTransform();
                transform.getTranslation(tmpV1);
                float angle = transform.getRotation(tmpQ).getAngleAround(Vector3.Y);
                tmpM.setToTranslation(tmpV1).rotate(Vector3.Y, angle).translate(0, 0, 20);
                camera.position.setZero().mul(tmpM);
                camera.lookAt(transform.getTranslation(tmpV1).add(0, 0, 0));
                camera.up.set(0, 1, 0);
                camera.update();
            }
        }
        public float getVelocity() {
            return getBody().getLinearVelocity().len();
        }
        public float getHeight() {
            return getTransform().getTranslation(tmpV1).y;
        }
        public Matrix4 getTransform() {
            return body.getComponent(Position.class).transform;
        }
        public btRigidBody getBody() {
            return body.getComponent(RigidBody.class).body;
        }
    }

    // ----- Private ----- //
    private static Entity addObject(String id, Matrix4 transform, Entity entity, Entity base, ConstraintSystem.Config constraint) {
        entity.setId(id);
        world.getEntityManager().addEntity(entity)
                .getComponent(Position.class).transform.set(transform);
        if (base != null) constraintSystem.addConstraint(base.getId(), id, constraint);
        return entity;
    }
    private static class Controller implements ConstraintSystem.Controller {
        private float low;
        private float high;
        private float resilience;
        private float target = 0;
        private boolean isRotated = false;
        private Controller(float low, float high, float resilience) {
            this.low = low;
            this.high = high;
            this.resilience = resilience;
        }
        private void rotate(float step) {
            isRotated = true;
            target += step;
        }
        @Override
        public void update(btTypedConstraint constraint) {
            if (!isRotated) {
                target += target > 0 ? -resilience : (target < 0 ? resilience : 0);
            }
            isRotated = false;
            target = Math.min(high, target);
            target = Math.max(low, target);
            btHingeConstraint hingeConstraint = (btHingeConstraint) constraint;
            hingeConstraint.setLimit(target, target, 0, 0.5f);
        }
    }
}
