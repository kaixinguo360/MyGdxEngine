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
import com.my.utils.world.com.*;
import com.my.utils.world.sys.CameraSystem;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;
import lombok.NoArgsConstructor;

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

        // ----- Variables ----- //
        private World world;
        private AssetsManager assetsManager;

        public AircraftBuilder(World world) {
            this.world = world;
            this.assetsManager = world.getAssetsManager();
        }

        // ----- Builder Methods ----- //

        private int bodyNum = 0;
        private Entity createBody(Matrix4 transform, Entity base) {
            String id = "Body-" + bodyNum++;
            return addObject(
                    id, transform, new MyInstance(assetsManager, "body", group),
                    base == null ? null : new Constraints.ConnectConstraint(base.getId(), id, null, 2000)
            );
        }

        private int wingNum = 0;
        private Entity createWing(Matrix4 transform, Entity base) {
            String id = "Wing-" + wingNum++;
            return addObject(
                    id, transform, new MyInstance(assetsManager, "wing", group, new Motions.Lift(new Vector3(0, 200, 0))),
                    base == null ? null : new Constraints.ConnectConstraint(base.getId(), id, null, 500)
            );
        }

        private int rotateNum = 0;
        private Entity createRotate(Matrix4 transform, Constraint.ConstraintController controller, Entity base) {
            Matrix4 relTransform = new Matrix4(base.getComponent(Position.class).transform).inv().mul(transform);
            String id = "Rotate-" + rotateNum++;
            Entity entity = addObject(
                    id, transform, new MyInstance(assetsManager, "rotate", group),
                    base == null ? null : new Constraints.HingeConstraint(
                            base.getId(), id, controller,
                            relTransform.rotate(Vector3.X, 90),
                            new Matrix4().rotate(Vector3.X, 90),
                            false)
            );
            return entity;
        }

        private int engineNum = 0;
        private Entity createEngine(Matrix4 transform, float force, float maxVelocity, Entity base) {
            String id = "Engine-" + engineNum++;
            return addObject(
                    id, transform,
                    new MyInstance(assetsManager, "engine", group, new Motions.LimitedForce(maxVelocity, new Vector3(0, force, 0), new Vector3())),
                    base == null ? null : new Constraints.ConnectConstraint(base.getId(), id, null, 2000)
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
            aircraft.aircraftController_L = new AircraftController(-0.15f, 0.2f, 0.5f);
            aircraft.rotate_L = createRotate(transform.cpy().translate(-1, 0.5f, -5).rotate(Vector3.Z, 90), aircraft.aircraftController_L, aircraft.body);
            aircraft.wing_L1 = createWing(transform.cpy().translate(-2.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraft.rotate_L);
            aircraft.wing_L2 = createWing(transform.cpy().translate(-4.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraft.wing_L1);

            // Right
            aircraft.aircraftController_R = new AircraftController(-0.15f, 0.2f, 0.5f);
            aircraft.rotate_R = createRotate(transform.cpy().translate(1, 0.5f, -5).rotate(Vector3.Z, 90), aircraft.aircraftController_R, aircraft.body);
            aircraft.wing_R1 = createWing(transform.cpy().translate(2.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraft.rotate_R);
            aircraft.wing_R2 = createWing(transform.cpy().translate(4.5f, 0.5f, -5).rotate(Vector3.X, 14), aircraft.wing_R1);

            // Horizontal Tail
            aircraft.aircraftController_T = new AircraftController(-0.2f, 0.2f, 1f);
            aircraft.rotate_T = createRotate(transform.cpy().translate(0, 0.5f, 0.1f).rotate(Vector3.Z, 90), aircraft.aircraftController_T, aircraft.body);
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

    public static class Aircraft implements CameraController, Component, LoadableResource {

        // ----- Temporary ----- //
        private static final Vector3 tmpV1 = new Vector3();
        private static final Matrix4 tmpM = new Matrix4();
        private static final Quaternion tmpQ = new Quaternion();;

        public Entity body;
        public Entity engine;
        public Entity rotate_L, rotate_R, rotate_T;
        public Entity wing_L1, wing_L2;
        public Entity wing_R1, wing_R2;
        public Entity wing_TL, wing_TR;
        public Entity wing_VL, wing_VR;

        public AircraftController aircraftController_L;
        public AircraftController aircraftController_R;
        public AircraftController aircraftController_T;

        public int bombNum;

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

        @Override
        public void load(Map<String, Object> config, LoadContext context) {
            EntityManager entityManager = context.getEnvironment("world", World.class).getEntityManager();
            Map<String, Object> map = config;
            body = entityManager.getEntity((String) map.get("body"));
            engine = entityManager.getEntity((String) map.get("engine"));
            rotate_L = entityManager.getEntity((String) map.get("rotate_L"));
            wing_L1 = entityManager.getEntity((String) map.get("wing_L1"));
            wing_L2 = entityManager.getEntity((String) map.get("wing_L2"));
            rotate_R = entityManager.getEntity((String) map.get("rotate_R"));
            wing_R1 = entityManager.getEntity((String) map.get("wing_R1"));
            wing_R2 = entityManager.getEntity((String) map.get("wing_R2"));
            rotate_T = entityManager.getEntity((String) map.get("rotate_T"));
            wing_TL = entityManager.getEntity((String) map.get("wing_TL"));
            wing_TR = entityManager.getEntity((String) map.get("wing_TR"));
            wing_VL = entityManager.getEntity((String) map.get("wing_VL"));
            wing_VR = entityManager.getEntity((String) map.get("wing_VR"));
            // TODO: Optimize Constraint Component
            if (rotate_L.contains(Constraint.class)) aircraftController_L = (AircraftController) rotate_L.getComponents(Constraint.class).get(0).controller;
            if (rotate_R.contains(Constraint.class)) aircraftController_R = (AircraftController) rotate_R.getComponents(Constraint.class).get(0).controller;
            if (rotate_T.contains(Constraint.class)) aircraftController_T = (AircraftController) rotate_T.getComponents(Constraint.class).get(0).controller;
            bombNum = (Integer) map.get("bombNum");
        }

        @Override
        public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
            return new HashMap<String, Object>() {{
                put("body", body.getId());
                put("engine", engine.getId());
                put("rotate_L", rotate_L.getId());
                put("wing_L1", wing_L1.getId());
                put("wing_L2", wing_L2.getId());
                put("rotate_R", rotate_R.getId());
                put("wing_R1", wing_R1.getId());
                put("wing_R2", wing_R2.getId());
                put("rotate_T", rotate_T.getId());
                put("wing_TL", wing_TL.getId());
                put("wing_TR", wing_TR.getId());
                put("wing_VL", wing_VL.getId());
                put("wing_VR", wing_VR.getId());
                put("bombNum", bombNum);
            }};
        }
    }

    public static class AircraftScript extends Script {

        private World world;
        private AssetsManager assetsManager;
        private PhysicsSystem physicsSystem;
        private Aircrafts.Aircraft aircraft;

        @Override
        public void init(World world, Entity entity) {
            this.world = world;
            this.assetsManager = world.getAssetsManager();
            this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
            this.aircraft = entity.getComponent(Aircrafts.Aircraft.class);
        }

        @Override
        public void execute(World world, Entity entity) {
            update();
        }

        // ----- Constants ----- //
        private final static short BOMB_FLAG = 1 << 8;
        private final static short AIRCRAFT_FLAG = 1 << 9;
        private final static short ALL_FLAG = -1;

        // ----- Temporary ----- //
        private static final Vector3 tmpV1 = new Vector3();
        private static final Matrix4 tmpM = new Matrix4();
        private static final Quaternion tmpQ = new Quaternion();

        public void update() {
            float v1 = 1f;
            float v2 = 0.5f;
            if (aircraft.aircraftController_L != null && aircraft.aircraftController_R != null) {
                if (Gdx.input.isKeyPressed(Input.Keys.W)) aircraft.aircraftController_T.rotate(v1);
                if (Gdx.input.isKeyPressed(Input.Keys.S)) aircraft.aircraftController_T.rotate(-v1);
                if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                    aircraft.aircraftController_L.rotate(v2);
                    aircraft.aircraftController_R.rotate(-v2);
                }
                if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                    aircraft.aircraftController_L.rotate(-v2);
                    aircraft.aircraftController_R.rotate(v2);
                }
            }
            if (Gdx.input.isKeyPressed(Input.Keys.J)) fire();
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) explode();
        }
        public void fire() {
            tmpM.set(getTransform()).translate(0, 0, -20 + (float) (Math.random() * 15)).rotate(Vector3.X, 90);
            getTransform().getRotation(tmpQ);
            tmpV1.set(getBody().getLinearVelocity());
            tmpV1.add(new Vector3(0, 0, -1).mul(tmpQ).scl(2000));
            btRigidBody body = createBomb(tmpM).getComponent(RigidBody.class).body;
            body.setLinearVelocity(tmpV1);
            body.setCcdMotionThreshold(1e-7f);
            body.setCcdSweptSphereRadius(2);
        }
        public void explode() {
            System.out.println("Explosion!");
            // TODO: Optimize Constraint Component
            aircraft.body.removeComponent(Constraint.class);
            aircraft.engine.removeComponent(Constraint.class);
            aircraft.rotate_L.removeComponent(Constraint.class);
            aircraft.rotate_R.removeComponent(Constraint.class);
            aircraft.rotate_T.removeComponent(Constraint.class);
            aircraft.wing_L1.removeComponent(Constraint.class);
            aircraft.wing_L2.removeComponent(Constraint.class);
            aircraft.wing_R1.removeComponent(Constraint.class);
            aircraft.wing_R2.removeComponent(Constraint.class);
            aircraft.wing_TL.removeComponent(Constraint.class);
            aircraft.wing_TR.removeComponent(Constraint.class);
            aircraft.wing_VL.removeComponent(Constraint.class);
            aircraft.wing_VR.removeComponent(Constraint.class);
            physicsSystem.addExplosion(getTransform().getTranslation(tmpV1), 2000);
        }
        public Matrix4 getTransform() {
            return aircraft.body.getComponent(Position.class).transform;
        }
        public btRigidBody getBody() {
            return aircraft.body.getComponent(RigidBody.class).body;
        }
        public Entity createBomb(Matrix4 transform) {
            Entity entity = new MyInstance(assetsManager, "bomb", "bomb", null,
                    new Collisions.BombCollisionHandler(BOMB_FLAG, ALL_FLAG));
            entity.setId("Bomb-" + aircraft.bombNum++);
            world.getEntityManager().addEntity(entity).getComponent(Position.class).transform.set(transform);
            entity.addComponent(new Scripts.RemoveScript());
            return entity;
        }
        public void changeCamera() {
            Camera camera = aircraft.body.getComponent(Camera.class);
//            camera.followType = CameraSystem.FollowType.B;
            camera.layer = (camera.layer + 1) % 2;
            switch (camera.layer) {
                case 0: {
                    camera.startX = 0;
                    camera.startY = 0;
                    camera.endX = 1;
                    camera.endY = 1;
                    break;
                }
                case 1: {
                    camera.startX = 0;
                    camera.startY = 0.7f;
                    camera.endX = 0.3f;
                    camera.endY = 1;
                    break;
                }
            }
        }
        public void changeCameraFollowType() {
            Camera camera = aircraft.body.getComponent(Camera.class);
            switch (camera.followType) {
                case A: camera.followType = CameraSystem.FollowType.B; break;
                case B: camera.followType = CameraSystem.FollowType.A; break;
            }
        }
    }

    @NoArgsConstructor
    public static class AircraftController implements Constraint.ConstraintController, StandaloneResource {

        @Config public float low;
        @Config public float high;
        @Config public float resilience;
        @Config public float target = 0;
        @Config public boolean isRotated = false;

        private AircraftController(float low, float high, float resilience) {
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
