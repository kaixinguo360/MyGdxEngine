package com.my.game;

import com.badlogic.gdx.Gdx;
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
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.*;
import com.my.utils.world.sys.ConstraintSystem;
import com.my.utils.world.sys.PhysicsSystem;

public class AircraftBuilder {

    // ----- Temporary ----- //
    private static final Vector3 tmpV1 = new Vector3();
    private static final Vector3 tmpV2 = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();
    private static final Quaternion tmpQ = new Quaternion();
    private static final String group = "group";

    // ----- Constants ----- //
    private final static short BOMB_FLAG = 1 << 8;
    private final static short AIRCRAFT_FLAG = 1 << 9;
    private final static short ALL_FLAG = -1;

    // ----- Variables ----- //
    private static World world;
    private static PhysicsSystem physicsSystem;
    private static ConstraintSystem constraintSystem;

    // ----- Init ----- //
    private static ArrayMap<String, Model> models = new ArrayMap<>();
    public static void init(World world) {
        AircraftBuilder.world = world;
        AircraftBuilder.physicsSystem = world.getSystem(PhysicsSystem.class);
        AircraftBuilder.constraintSystem = world.getSystem(ConstraintSystem.class);

        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();
        models.put("bomb", mdBuilder.createCapsule(0.5f, 2, 8, new Material(ColorAttribute.createDiffuse(Color.GRAY)), attributes));
        models.put("body", mdBuilder.createBox(1, 1, 5, new Material(ColorAttribute.createDiffuse(Color.GREEN)), attributes));
        models.put("wing", mdBuilder.createBox(2, 0.2f, 1, new Material(ColorAttribute.createDiffuse(Color.BLUE)), attributes));
        models.put("rotate", mdBuilder.createCylinder(1, 1, 1, 8, new Material(ColorAttribute.createDiffuse(Color.CYAN)), attributes));
        models.put("engine", mdBuilder.createCone(0.9f, 1, 0.9f, 18, new Material(ColorAttribute.createDiffuse(Color.YELLOW)), attributes));

        Render.addConfig("bomb", new Render.Config(models.get("bomb")));
        Render.addConfig("body", new Render.Config(models.get("body")));
        Render.addConfig("wing", new Render.Config(models.get("wing")));
        Render.addConfig("rotate", new Render.Config(models.get("rotate")));
        Render.addConfig("engine", new Render.Config(models.get("engine")));

        RigidBody.addConfig("bomb", new RigidBody.Config(new btCapsuleShape(0.5f, 1), 50f));
        RigidBody.addConfig("body", new RigidBody.Config(new btBoxShape(new Vector3(0.5f,0.5f,2.5f)), 50f));
        RigidBody.addConfig("wing", new RigidBody.Config(new btBoxShape(new Vector3(1f,0.1f,0.5f)), 25f));
        RigidBody.addConfig("rotate", new RigidBody.Config(new btCylinderShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        RigidBody.addConfig("engine", new RigidBody.Config(new btConeShape(0.45f,1), 50));

    }

    // ----- Builder Methods ----- //
    private static int bombNum = 0;
    public static Entity createBomb(Matrix4 transform, Entity base) {
        Entity entity = new MyInstance("bomb", "bomb", null,
                new Collision(BOMB_FLAG, ALL_FLAG, (self, target) -> {
                    if (checkVelocity(self, target, 20)) {
                        System.out.println("Boom! " + self.get(Id.class) + " ==> " + target.get(Id.class));
                        physicsSystem.addExplosion(self.get(Position.class).transform.getTranslation(tmpV1), 5000);
                        world.removeEntity(self);
                    }
                }));
        addObject(
                "Bomb-" + bombNum++,
                transform,
                entity,
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
        return entity;
    }

    private static int bodyNum = 0;
    public static Entity createBody(Matrix4 transform, Entity base) {
        return addObject(
                "Body-" + bodyNum++,
                transform,
                new MyInstance("body", group),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
    }

    private static int wingNum = 0;
    public static Entity createWing(Matrix4 transform, Entity base) {
        return addObject(
                "Wing-" + wingNum++,
                transform,
                new MyInstance("wing", group, new Motion.Lift(new Vector3(0, 200, 0)),
                        new Collision(AIRCRAFT_FLAG, ALL_FLAG, AircraftBuilder::collide)),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint(500)
        );
    }

    private static int rotate = 0;
    public static Entity createRotate(Matrix4 transform, ConstraintSystem.Controller controller, Entity base) {
        Matrix4 relTransform = new Matrix4(base.get(Position.class).transform).inv().mul(transform);
        Entity entity = addObject(
                "Rotate-" + rotate++,
                transform,
                new MyInstance("rotate", group, null,
                        new Collision(AIRCRAFT_FLAG, ALL_FLAG, AircraftBuilder::collide)),
                base,
                base == null ? null : new ConstraintSystem.HingeConstraint(
                        relTransform.rotate(Vector3.X, 90),
                        new Matrix4().rotate(Vector3.X, 90),
                        false)
        );
        constraintSystem.addController(entity.get(Id.class).id, base.get(Id.class).id, controller);
        return entity;
    }

    private static int engineNum = 0;
    public static Entity createEngine(Matrix4 transform, float force, float maxVelocity, Entity base) {
        return addObject(
                "Engine-" + engineNum++,
                transform,
                new MyInstance("engine", group, new Motion.LimitedForce(maxVelocity, new Vector3(0, force, 0)),
                        new Collision(AIRCRAFT_FLAG, ALL_FLAG, AircraftBuilder::collide)),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
    }

    public static Aircraft createAircraft(Matrix4 transform, float force, float maxVelocity, int upKey, int downKey, int leftKey, int rightKey) {
        return new Aircraft(transform, force, maxVelocity, upKey, downKey, leftKey, rightKey);
    }

    public static class Aircraft {

        private Entity body;
        private Entity engine;
        private Entity rotate_L, rotate_R, rotate_T;
        private Entity wing_L1, wing_L2;
        private Entity wing_R1, wing_R2;
        private Entity wing_TL, wing_TR;
        private Entity wing_VL, wing_VR;

        public Aircraft(Matrix4 transform, float force, float maxVelocity, int upKey, int downKey, int leftKey, int rightKey) {

            // Body
            body = createBody(transform.cpy().translate(0, 0.5f, -3), null);
            engine = createEngine(transform.cpy().translate(0, 0.6f, -6).rotate(Vector3.X, -90), force, maxVelocity, body);

            // Left
            rotate_L = createRotate(transform.cpy().translate(-1, 0.5f, -5).rotate(Vector3.Z, 90),
                    new Controller(-0.15f, 0.2f, 0.5f, rightKey, leftKey), body);
            wing_L1 = createWing(transform.cpy().translate(-2.5f, 0.5f, -5).rotate(Vector3.X, 14), rotate_L);
            wing_L2 = createWing(transform.cpy().translate(-4.5f, 0.5f, -5).rotate(Vector3.X, 14), wing_L1);

            // Right
            rotate_R = createRotate(transform.cpy().translate(1, 0.5f, -5).rotate(Vector3.Z, 90),
                    new Controller(-0.15f, 0.2f, 0.5f, leftKey, rightKey), body);
            wing_R1 = createWing(transform.cpy().translate(2.5f, 0.5f, -5).rotate(Vector3.X, 14), rotate_R);
            wing_R2 = createWing(transform.cpy().translate(4.5f, 0.5f, -5).rotate(Vector3.X, 14), wing_R1);

            // Horizontal Tail
            rotate_T = createRotate(transform.cpy().translate(0, 0.5f, 0.1f).rotate(Vector3.Z, 90),
                    new Controller(-0.2f, 0.2f, 1f, downKey, upKey), body);
            wing_TL = createWing(transform.cpy().translate(-1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), rotate_T);
            wing_TR = createWing(transform.cpy().translate(1.5f, 0.5f, 0.1f).rotate(Vector3.X, 13f), rotate_T);

            // Vertical Tail
            wing_VL = createWing(transform.cpy().translate(-0.6f, 1f, -1).rotate(Vector3.Z, 90), body);
            wing_VR = createWing(transform.cpy().translate(0.6f, 1f, -1).rotate(Vector3.Z, 90), body);
        }

        public void fire() {
            tmpM.set(getTransform()).translate(0, 0, -6.5f).rotate(Vector3.X, 90);
            getTransform().getRotation(tmpQ);
            tmpV1.set(getBody().getLinearVelocity());
            tmpV1.add(new Vector3(0, 0, -1).mul(tmpQ).scl(2000));
            btRigidBody body = createBomb(tmpM, null).get(RigidBody.class).body;
            body.setLinearVelocity(tmpV1);
            body.setCcdMotionThreshold(1e-7f);
            body.setCcdSweptSphereRadius(2);
        }
        public void explode() {
            System.out.println("Explosion!");
            constraintSystem.remove(world, body.get(Id.class).id);
            constraintSystem.remove(world, engine.get(Id.class).id);
            constraintSystem.remove(world, rotate_L.get(Id.class).id);
            constraintSystem.remove(world, rotate_R.get(Id.class).id);
            constraintSystem.remove(world, rotate_T.get(Id.class).id);
            constraintSystem.remove(world, wing_L1.get(Id.class).id);
            constraintSystem.remove(world, wing_L2.get(Id.class).id);
            constraintSystem.remove(world, wing_R1.get(Id.class).id);
            constraintSystem.remove(world, wing_R2.get(Id.class).id);
            constraintSystem.remove(world, wing_TL.get(Id.class).id);
            constraintSystem.remove(world, wing_TR.get(Id.class).id);
            constraintSystem.remove(world, wing_VL.get(Id.class).id);
            constraintSystem.remove(world, wing_VR.get(Id.class).id);
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
            return body.get(Position.class).transform;
        }
        public btRigidBody getBody() {
            return body.get(RigidBody.class).body;
        }
    }

    // ----- Private ----- //
    private static Entity addObject(String id, Matrix4 transform, Entity entity, Entity base, ConstraintSystem.Config constraint) {
        world.addEntity(id, entity)
                .get(Position.class).transform.set(transform);
        if (base != null) constraintSystem.addConstraint(base.get(Id.class).id, id, constraint);
        return entity;
    }
    private static void collide(Entity self, Entity target) {
        if (checkVelocity(self, target, 40)) {
            System.out.println("Collision!");
//            constraintSystem.remove(world, self.get(Id.class).id);
        }
    }
    private static boolean checkVelocity(Entity self, Entity target, double maxVelocity) {
        tmpV1.set(self.get(RigidBody.class).body.getLinearVelocity());
        tmpV2.set(target.get(RigidBody.class).body.getLinearVelocity());
        return tmpV1.sub(tmpV2).len() > maxVelocity;
    }
    private static class Controller implements ConstraintSystem.Controller {
        private float low;
        private float high;
        private float delta;
        private int down;
        private int up;
        private float target = 0;
        private Controller(float low, float high, float delta, int down, int up) {
            this.low = low;
            this.high = high;
            this.delta = delta;
            this.down = down;
            this.up = up;
        }
        @Override
        public void update(btTypedConstraint constraint) {
            if (Gdx.input.isKeyPressed(up)) {
                target += delta;
            } else if (Gdx.input.isKeyPressed(down)) {
                target -= delta;
            } else {
                target += target > 0 ? -delta : (target < 0 ? delta : 0);
            }
            target = target > high ? high : (target < low ? low : target);
            btHingeConstraint hingeConstraint = (btHingeConstraint) constraint;
            hingeConstraint.setLimit(target, target, 0, 0.5f);
        }
    }
}
