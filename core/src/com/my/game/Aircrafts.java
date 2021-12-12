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
import com.my.utils.world.*;
import com.my.utils.world.com.Collision;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.com.ScriptComponent;
import com.my.utils.world.sys.ConstraintSystem;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;
import com.my.utils.world.sys.ScriptSystem;

import java.lang.System;
import java.util.HashMap;
import java.util.Map;

public class Aircrafts {

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

    public static class AircraftBuilder {

        // ----- Constants ----- //
        private static final String group = "group";
        private final static short BOMB_FLAG = 1 << 8;
        private final static short AIRCRAFT_FLAG = 1 << 9;
        private final static short ALL_FLAG = -1;

        // ----- Variables ----- //
        private World world;
        private AssetsManager assetsManager;
        private PhysicsSystem physicsSystem;
        private ConstraintSystem constraintSystem;

        public AircraftBuilder(World world) {
            this.world = world;
            this.assetsManager = world.getAssetsManager();
            this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
            this.constraintSystem = world.getSystemManager().getSystem(ConstraintSystem.class);
        }

        // ----- Builder Methods ----- //

        private int bombNum = 0;
        public Entity createBomb(Matrix4 transform, Entity base) {
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

        private int bodyNum = 0;
        private Entity createBody(Matrix4 transform, Entity base) {
            return addObject(
                    "Body-" + bodyNum++,
                    transform,
                    new MyInstance("body", group),
                    base,
                    base == null ? null : new ConstraintSystem.ConnectConstraint()
            );
        }

        private int wingNum = 0;
        private Entity createWing(Matrix4 transform, Entity base) {
            return addObject(
                    "Wing-" + wingNum++,
                    transform,
                    new MyInstance("wing", group, Motions.Lift.getConfig(assetsManager, new Vector3(0, 200, 0))),
                    base,
                    base == null ? null : new ConstraintSystem.ConnectConstraint(500)
            );
        }

        private int rotateNum = 0;
        private Entity createRotate(Matrix4 transform, ConstraintSystem.Controller controller, Entity base) {
            Matrix4 relTransform = new Matrix4(base.getComponent(Position.class).transform).inv().mul(transform);
            Entity entity = addObject(
                    "Rotate-" + rotateNum++,
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

        private int engineNum = 0;
        private Entity createEngine(Matrix4 transform, float force, float maxVelocity, Entity base) {
            return addObject(
                    "Engine-" + engineNum++,
                    transform,
                    new MyInstance("engine", group, Motions.LimitedForce.getConfig(assetsManager, maxVelocity, new Vector3(0, force, 0), new Vector3())),
                    base,
                    base == null ? null : new ConstraintSystem.ConnectConstraint()
            );
        }

        private int aircraftNum = 0;
        public Entity createAircraft(Matrix4 transform, float force, float maxVelocity) {

            // Aircraft
            Aircraft aircraft = new Aircraft();

            // Body
            aircraft.body = createBody(transform.cpy().translate(0, 0.5f, -3), null);
            aircraft.engine = createEngine(transform.cpy().translate(0, 0.6f, -6).rotate(Vector3.X, -90), force, maxVelocity, aircraft.body);

            // Left
            aircraft.rotate_L = createRotate(transform.cpy().translate(-1, 0.5f, -5).rotate(Vector3.Z, 90), aircraft.controller_L, aircraft.body);
            aircraft.wing_L1 = createWing(transform.cpy().translate(-2.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraft.rotate_L);
            aircraft.wing_L2 = createWing(transform.cpy().translate(-4.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraft.wing_L1);

            // Right
            aircraft.rotate_R = createRotate(transform.cpy().translate(1, 0.5f, -5).rotate(Vector3.Z, 90), aircraft.controller_R, aircraft.body);
            aircraft.wing_R1 = createWing(transform.cpy().translate(2.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraft.rotate_R);
            aircraft.wing_R2 = createWing(transform.cpy().translate(4.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraft.wing_R1);

            // Horizontal Tail
            aircraft.rotate_T = createRotate(transform.cpy().translate(0, 0.5f, 0.1f).rotate(Vector3.Z, 90), aircraft.controller_T, aircraft.body);
            aircraft.wing_TL = createWing(transform.cpy().translate(-1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), aircraft.rotate_T);
            aircraft.wing_TR = createWing(transform.cpy().translate(1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), aircraft.rotate_T);

            // Vertical Tail
            aircraft.wing_VL = createWing(transform.cpy().translate(-0.6f, 1f, -1).rotate(Vector3.Z, 90), aircraft.body);
            aircraft.wing_VR = createWing(transform.cpy().translate(0.6f, 1f, -1).rotate(Vector3.Z, 90), aircraft.body);

            // Aircraft Entity
            Entity entity = new Entity();
            entity.setId("Aircraft-" + aircraftNum++);
            entity.addComponent(aircraft);
            world.getEntityManager().addEntity(entity);

            return entity;
        }

        // ----- Private ----- //
        private Entity addObject(String id, Matrix4 transform, Entity entity, Entity base, ConstraintSystem.Config constraint) {
            entity.setId(id);
            world.getEntityManager().addEntity(entity)
                    .getComponent(Position.class).transform.set(transform);
            if (base != null) constraintSystem.addConstraint(base.getId(), id, constraint);
            return entity;
        }
    }

    public static class Aircraft implements CameraController, Component {

        // ----- Temporary ----- //
        private static final Vector3 tmpV1 = new Vector3();
        private static final Matrix4 tmpM = new Matrix4();
        private static final Quaternion tmpQ = new Quaternion();

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

        @Override
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

    public static class AircraftScript implements ScriptSystem.Script, AfterAdded {

        private World world;
        private PhysicsSystem physicsSystem;
        private ConstraintSystem constraintSystem;
        private Aircrafts.AircraftBuilder aircraftBuilder;

        @Override
        public void afterAdded(World world) {
            this.world = world;
            this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
            this.constraintSystem = world.getSystemManager().getSystem(ConstraintSystem.class);
            this.aircraftBuilder = new Aircrafts.AircraftBuilder(world);
        }

        @Override
        public void init(World world, Entity entity, ScriptComponent scriptComponent) {
            scriptComponent.customObj = entity.getComponent(Aircrafts.Aircraft.class);
            Aircrafts.Aircraft aircraft = entity.getComponent(Aircrafts.Aircraft.class);
        }

        @Override
        public void execute(World world, Entity entity, ScriptComponent scriptComponent) {
            Aircrafts.Aircraft aircraft = (Aircrafts.Aircraft) scriptComponent.customObj;
            update(aircraft);
        }

        // ----- Temporary ----- //
        private static final Vector3 tmpV1 = new Vector3();
        private static final Matrix4 tmpM = new Matrix4();
        private static final Quaternion tmpQ = new Quaternion();

        public void update(Aircrafts.Aircraft aircraft) {
            float v1 = 1f;
            float v2 = 0.5f;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) aircraft.controller_T.rotate(v1);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) aircraft.controller_T.rotate(-v1);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                aircraft.controller_L.rotate(v2);
                aircraft.controller_R.rotate(-v2);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                aircraft.controller_L.rotate(-v2);
                aircraft.controller_R.rotate(v2);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.J)) fire(aircraft);
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) explode(aircraft);
        }
        public void fire(Aircrafts.Aircraft aircraft) {
            tmpM.set(getTransform(aircraft)).translate(0, 0, -20 + (float) (Math.random() * 15)).rotate(Vector3.X, 90);
            getTransform(aircraft).getRotation(tmpQ);
            tmpV1.set(getBody(aircraft).getLinearVelocity());
            tmpV1.add(new Vector3(0, 0, -1).mul(tmpQ).scl(2000));
            btRigidBody body = aircraftBuilder.createBomb(tmpM, null).getComponent(RigidBody.class).body;
            body.setLinearVelocity(tmpV1);
            body.setCcdMotionThreshold(1e-7f);
            body.setCcdSweptSphereRadius(2);
        }
        public void explode(Aircrafts.Aircraft aircraft) {
            System.out.println("Explosion!");
            constraintSystem.remove(world, aircraft.body.getId());
            constraintSystem.remove(world, aircraft.engine.getId());
            constraintSystem.remove(world, aircraft.rotate_L.getId());
            constraintSystem.remove(world, aircraft.rotate_R.getId());
            constraintSystem.remove(world, aircraft.rotate_T.getId());
            constraintSystem.remove(world, aircraft.wing_L1.getId());
            constraintSystem.remove(world, aircraft.wing_L2.getId());
            constraintSystem.remove(world, aircraft.wing_R1.getId());
            constraintSystem.remove(world, aircraft.wing_R2.getId());
            constraintSystem.remove(world, aircraft.wing_TL.getId());
            constraintSystem.remove(world, aircraft.wing_TR.getId());
            constraintSystem.remove(world, aircraft.wing_VL.getId());
            constraintSystem.remove(world, aircraft.wing_VR.getId());
            physicsSystem.addExplosion(getTransform(aircraft).getTranslation(tmpV1), 2000);
        }
        public Matrix4 getTransform(Aircrafts.Aircraft aircraft) {
            return aircraft.body.getComponent(Position.class).transform;
        }
        public btRigidBody getBody(Aircrafts.Aircraft aircraft) {
            return aircraft.body.getComponent(RigidBody.class).body;
        }
    }

    public static class AircraftLoader implements Loader {

        private LoaderManager loaderManager;
        private EntityManager entityManager;

        public AircraftLoader(LoaderManager loaderManager) {
            this.loaderManager = loaderManager;
        }

        private EntityManager getEntityManager() {
            World world = (World) loaderManager.getEnvironment().get("world");
            if (world == null) throw new RuntimeException("Required params not set: world");
            return world.getEntityManager();
        }

        @Override
        public <E, T> T load(E config, Class<T> type) {
            if (entityManager == null) entityManager = getEntityManager();
            Map<String, Object> map = (Map<String, Object>) config;
            Aircraft aircraft = new Aircraft();
            aircraft.body = entityManager.getEntity((String) map.get("body"));
            aircraft.engine = entityManager.getEntity((String) map.get("engine"));
            aircraft.rotate_L = entityManager.getEntity((String) map.get("rotate_L"));
            aircraft.wing_L1 = entityManager.getEntity((String) map.get("wing_L1"));
            aircraft.wing_L2 = entityManager.getEntity((String) map.get("wing_L2"));
            aircraft.rotate_R = entityManager.getEntity((String) map.get("rotate_R"));
            aircraft.wing_R1 = entityManager.getEntity((String) map.get("wing_R1"));
            aircraft.wing_R2 = entityManager.getEntity((String) map.get("wing_R2"));
            aircraft.rotate_T = entityManager.getEntity((String) map.get("rotate_T"));
            aircraft.wing_TL = entityManager.getEntity((String) map.get("wing_TL"));
            aircraft.wing_TR = entityManager.getEntity((String) map.get("wing_TR"));
            aircraft.wing_VL = entityManager.getEntity((String) map.get("wing_VL"));
            aircraft.wing_VR = entityManager.getEntity((String) map.get("wing_VR"));
            return (T) aircraft;
        }

        @Override
        public <E, T> E getConfig(T obj, Class<E> configType) {
            if (entityManager == null) entityManager = getEntityManager();
            Aircraft aircraft = (Aircraft) obj;
            return (E) new HashMap<String, Object>() {{
                put("body", aircraft.body.getId());
                put("engine", aircraft.engine.getId());
                put("rotate_L", aircraft.rotate_L.getId());
                put("wing_L1", aircraft.wing_L1.getId());
                put("wing_L2", aircraft.wing_L2.getId());
                put("rotate_R", aircraft.rotate_R.getId());
                put("wing_R1", aircraft.wing_R1.getId());
                put("wing_R2", aircraft.wing_R2.getId());
                put("rotate_T", aircraft.rotate_T.getId());
                put("wing_TL", aircraft.wing_TL.getId());
                put("wing_TR", aircraft.wing_TR.getId());
                put("wing_VL", aircraft.wing_VL.getId());
                put("wing_VR", aircraft.wing_VR.getId());
            }};
        }

        @Override
        public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
            return (Map.class.isAssignableFrom(configType)) && (targetType == Aircraft.class);
        }
    }

    public static class Controller implements ConstraintSystem.Controller {
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
