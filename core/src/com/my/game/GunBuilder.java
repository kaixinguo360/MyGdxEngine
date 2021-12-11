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
import com.my.utils.world.com.Render;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.sys.ConstraintSystem;
import com.my.utils.world.sys.PhysicsSystem;

public class GunBuilder {

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
    private static AssetsManager assetsManager;
    private static PhysicsSystem physicsSystem;
    private static ConstraintSystem constraintSystem;

    // ----- Init ----- //
    private static ArrayMap<String, Model> models = new ArrayMap<>();
    public static void init(World world) {
        GunBuilder.world = world;
        GunBuilder.assetsManager = world.getAssetsManager();
        GunBuilder.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
        GunBuilder.constraintSystem = world.getSystemManager().getSystem(ConstraintSystem.class);

        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();
        models.put("bullet", mdBuilder.createCapsule(0.5f, 2, 8, new Material(ColorAttribute.createDiffuse(Color.YELLOW)), VertexAttributes.Usage.Position));
        models.put("barrel", mdBuilder.createBox(1, 1, 5, new Material(ColorAttribute.createDiffuse(Color.GREEN)), attributes));
        models.put("gunRotate", mdBuilder.createCylinder(1, 1, 1, 8, new Material(ColorAttribute.createDiffuse(Color.CYAN)), attributes));

        Render.addConfig("bullet", new Render.Config(models.get("bullet")));
        Render.addConfig("barrel", new Render.Config(models.get("barrel")));
        Render.addConfig("gunRotate", new Render.Config(models.get("gunRotate")));

        initAssets(assetsManager);
    }

    public static void initAssets(AssetsManager assetsManager) {
        assetsManager.addAsset("bullet", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btCapsuleShape(0.5f, 1), 50f));
        assetsManager.addAsset("barrel", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btBoxShape(new Vector3(0.5f,0.5f,2.5f)), 5f));
        assetsManager.addAsset("gunRotate", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btCylinderShape(new Vector3(0.5f,0.5f,0.5f)), 50f));

    }

    // ----- Builder Methods ----- //
    private static int bulletNum = 0;
    public static Entity createBullet(Matrix4 transform, Entity base) {
        Entity entity = new MyInstance("bullet", "bullet", null,
                new Collision(BOMB_FLAG, ALL_FLAG, assetsManager.getAsset("BulletCollisionHandler", PhysicsSystem.CollisionHandler.class)));
        addObject(
                "Bullet-" + bulletNum++,
                transform,
                entity,
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
        return entity;
    }

    private static int barrelNum = 0;
    public static Entity createBarrel(Matrix4 transform, Entity base) {
        return addObject(
                "Barrel-" + barrelNum++,
                transform,
                new MyInstance("barrel", group),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
    }

    private static int gunRotateNum = 0;
    public static Entity createRotate(Matrix4 transform, ConstraintSystem.Controller controller, Entity base) {
        Matrix4 relTransform = new Matrix4(base.getComponent(Position.class).transform).inv().mul(transform);
        Entity entity = addObject(
                "GunRotate-" + gunRotateNum++,
                transform,
                new MyInstance("gunRotate", group),
                base,
                base == null ? null : new ConstraintSystem.HingeConstraint(
                        relTransform.rotate(Vector3.X, 90),
                        new Matrix4().rotate(Vector3.X, 90),
                        false)
        );
        constraintSystem.addController(entity.getId(), base.getId(), controller);
        return entity;
    }

    public static Gun createGun(Matrix4 transform) {
        return new Gun(transform);
    }

    public static class Gun implements Controllable {

        private Entity rotate_Y, rotate_X, barrel;
        private Controller controller_X = new Controller(-90, 0);
        private Controller controller_Y = new Controller();

        public Gun(Matrix4 transform) {
            rotate_Y = createRotate(transform.cpy().translate(0, 0.5f, 0),
                    controller_Y, world.getEntityManager().getEntity("ground"));
            rotate_X = createRotate(transform.cpy().translate(0, 1.5f, 0).rotate(Vector3.Z, 90),
                    controller_X, rotate_Y);
            barrel = createBarrel(transform.cpy().translate(0, 1.5f, -3), rotate_X);
        }

        public void update() {
            float v = 0.025f;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) rotate(0, -v);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) rotate(0, v);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) rotate(v, 0);
            if (Gdx.input.isKeyPressed(Input.Keys.D)) rotate(-v, 0);
            if (Gdx.input.isKeyPressed(Input.Keys.J)) fire();
        }
        public void fire() {
            tmpM.set(getTransform()).translate(0, 0, -20 + (float) (Math.random() * 15)).rotate(Vector3.X, 90);
            getTransform().getRotation(tmpQ);
            tmpV1.set(getBody().getLinearVelocity());
            tmpV1.add(new Vector3(0, 0, -1).mul(tmpQ).scl(2000));
            btRigidBody body = createBullet(tmpM, null).getComponent(RigidBody.class).body;
            body.setLinearVelocity(tmpV1);
            body.setCcdMotionThreshold(1e-7f);
            body.setCcdSweptSphereRadius(2);
        }
        public void explode() {
            System.out.println("Explosion!");
            constraintSystem.remove(world, rotate_Y.getId());
            constraintSystem.remove(world, rotate_X.getId());
            constraintSystem.remove(world, barrel.getId());
            physicsSystem.addExplosion(getTransform().getTranslation(tmpV1), 2000);
        }
        public void rotate(float stepY, float stepX) {
            setDirection(controller_Y.target + stepY, controller_X.target + stepX);
        }
        public void setDirection(float angleY, float angleX) {
            getBody().activate();
            controller_Y.target = angleY;
            controller_X.target = angleX;
        }
        public void setCamera(PerspectiveCamera camera, int index) {
            if (index == 0) {
                Matrix4 transform = getTransform();
                camera.position.set(0, 1, 0).mul(transform);
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
        public Matrix4 getTransform() {
            return barrel.getComponent(Position.class).transform;
        }
        public btRigidBody getBody() {
            return barrel.getComponent(RigidBody.class).body;
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
    private static boolean checkVelocity(Entity self, Entity target, double maxVelocity) {
        tmpV1.set(self.getComponent(RigidBody.class).body.getLinearVelocity());
        tmpV2.set(target.getComponent(RigidBody.class).body.getLinearVelocity());
        return tmpV1.sub(tmpV2).len() > maxVelocity;
    }
    private static class Controller implements ConstraintSystem.Controller {
        private float target = 0;
        private float max = 0;
        private float min = 0;
        private boolean limit = false;
        private Controller() {}
        private Controller(float min, float max) {
            limit = true;
            this.min = (float) Math.toRadians(min);
            this.max = (float) Math.toRadians(max);
        }
        @Override
        public void update(btTypedConstraint constraint) {
            if (limit) {
                target = Math.min(max, target);
                target = Math.max(min, target);
            }
            btHingeConstraint hingeConstraint = (btHingeConstraint) constraint;
            hingeConstraint.setLimit(target, target, 0, 0.5f);
        }
    }
}
