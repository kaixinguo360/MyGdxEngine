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

public class Guns {

    public static void initAssets(AssetsManager assetsManager) {
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();
        ArrayMap<String, Model> models = new ArrayMap<>();

        models.put("bullet", mdBuilder.createCapsule(0.5f, 2, 8, new Material(ColorAttribute.createDiffuse(Color.YELLOW)), VertexAttributes.Usage.Position));
        models.put("barrel", mdBuilder.createBox(1, 1, 5, new Material(ColorAttribute.createDiffuse(Color.GREEN)), attributes));
        models.put("gunRotate", mdBuilder.createCylinder(1, 1, 1, 8, new Material(ColorAttribute.createDiffuse(Color.CYAN)), attributes));

        assetsManager.addAsset("bullet", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("bullet")));
        assetsManager.addAsset("barrel", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("barrel")));
        assetsManager.addAsset("gunRotate", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("gunRotate")));

        assetsManager.addAsset("bullet", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btCapsuleShape(0.5f, 1), 50f));
        assetsManager.addAsset("barrel", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btBoxShape(new Vector3(0.5f,0.5f,2.5f)), 5f));
        assetsManager.addAsset("gunRotate", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btCylinderShape(new Vector3(0.5f,0.5f,0.5f)), 50f));

    }

    public static class GunBuilder {

        // ----- Constants ----- //
        private final static short BOMB_FLAG = 1 << 8;
        private final static short GUN_FLAG = 1 << 9;
        private final static short ALL_FLAG = -1;

        private static final String group = "group";
        // ----- Variables ----- //
        private World world;
        private AssetsManager assetsManager;
        private PhysicsSystem physicsSystem;
        private ConstraintSystem constraintSystem;

        // ----- Init ----- //
        public GunBuilder(World world) {
            this.world = world;
            this.assetsManager = world.getAssetsManager();
            this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
            this.constraintSystem = world.getSystemManager().getSystem(ConstraintSystem.class);
        }

        // ----- Builder Methods ----- //

        private int bulletNum = 0;
        private Entity createBullet(Matrix4 transform, Entity base) {
            Entity entity = new MyInstance("bullet", "bullet", null,
                    new Collision(BOMB_FLAG, ALL_FLAG, assetsManager.getAsset("BulletCollisionHandler", PhysicsSystem.CollisionHandler.class)));
            addObject(
                    "Bullet-" + bulletNum++,
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

        private int barrelNum = 0;
        private Entity createBarrel(Matrix4 transform, Entity base) {
            return addObject(
                    "Barrel-" + barrelNum++,
                    transform,
                    new MyInstance("barrel", group),
                    base,
                    base == null ? null : new ConstraintSystem.ConnectConstraint()
            );
        }

        private int rotateNum = 0;
        private Entity createRotate(Matrix4 transform, ConstraintSystem.Controller controller, Entity base) {
            Matrix4 relTransform = new Matrix4(base.getComponent(Position.class).transform).inv().mul(transform);
            Entity entity = addObject(
                    "GunRotate-" + rotateNum++,
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

        private int gunNum = 0;
        public Gun createGun(String baseObjectId, Matrix4 transform) {

            // Gun
            Guns.Gun gun = new Guns.Gun();

            gun.rotate_Y = createRotate(transform.cpy().translate(0, 0.5f, 0), gun.controller_Y, world.getEntityManager().getEntity(baseObjectId));
            gun.rotate_X = createRotate(transform.cpy().translate(0, 1.5f, 0).rotate(Vector3.Z, 90), gun.controller_X, gun.rotate_Y);
            gun.barrel = createBarrel(transform.cpy().translate(0, 1.5f, -3), gun.rotate_X);

            // Gun Entity
            Entity entity = new Entity();
            entity.setId("Gun-" + gunNum++);
            entity.addComponent(gun);
            world.getEntityManager().addEntity(entity);

            return gun;
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

    public static class Gun implements Controllable, Component {

        // ----- Temporary ----- //
        private static final Vector3 tmpV = new Vector3();
        private static final Matrix4 tmpM = new Matrix4();
        private static final Quaternion tmpQ = new Quaternion();

        private Entity rotate_Y, rotate_X, barrel;
        private Controller controller_X = new Controller(-90, 0);
        private Controller controller_Y = new Controller();
        private World world;
        private PhysicsSystem physicsSystem;
        private ConstraintSystem constraintSystem;
        private GunBuilder gunBuilder;

        private Gun() {}

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
            tmpV.set(getBody().getLinearVelocity());
            tmpV.add(new Vector3(0, 0, -1).mul(tmpQ).scl(2000));
            btRigidBody body = gunBuilder.createBullet(tmpM, null).getComponent(RigidBody.class).body;
            body.setLinearVelocity(tmpV);
            body.setCcdMotionThreshold(1e-7f);
            body.setCcdSweptSphereRadius(2);
        }
        public void explode() {
            System.out.println("Explosion!");
            constraintSystem.remove(world, rotate_Y.getId());
            constraintSystem.remove(world, rotate_X.getId());
            constraintSystem.remove(world, barrel.getId());
            physicsSystem.addExplosion(getTransform().getTranslation(tmpV), 2000);
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
                transform.getTranslation(tmpV);
                float angle = transform.getRotation(tmpQ).getAngleAround(Vector3.Y);
                tmpM.setToTranslation(tmpV).rotate(Vector3.Y, angle).translate(0, 0, 20);
                camera.position.setZero().mul(tmpM);
                camera.lookAt(transform.getTranslation(tmpV).add(0, 0, 0));
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

    public static class GunSystem extends BaseSystem implements EntityListener {

        @Override
        public void afterAdded(Entity entity) {
            Guns.Gun gun = entity.getComponent(Guns.Gun.class);
            gun.world = world;
            gun.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
            gun.constraintSystem = world.getSystemManager().getSystem(ConstraintSystem.class);
            gun.gunBuilder = new Guns.GunBuilder(world);
        }

        @Override
        public void afterRemoved(Entity entity) {

        }

        @Override
        protected boolean isHandleable(Entity entity) {
            return entity.contain(Guns.Gun.class);
        }
    }

    public static class GunLoader implements Loader {

        private LoaderManager loaderManager;
        private EntityManager entityManager;

        public GunLoader(LoaderManager loaderManager) {
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
            Map<String, String> map = (Map<String, String>) config;
            Guns.Gun gun = new Guns.Gun();
            gun.rotate_Y = entityManager.getEntity(map.get("rotate_Y"));
            gun.rotate_X = entityManager.getEntity(map.get("rotate_X"));
            gun.barrel = entityManager.getEntity(map.get("barrel"));
            return (T) gun;
        }

        @Override
        public <E, T> E getConfig(T obj, Class<E> configType) {
            if (entityManager == null) entityManager = getEntityManager();
            Guns.Gun gun = (Guns.Gun) obj;
            return (E) new HashMap<String, String>() {{
                put("rotate_Y", gun.rotate_Y.getId());
                put("rotate_X", gun.rotate_X.getId());
                put("barrel", gun.barrel.getId());
            }};
        }

        @Override
        public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
            return (Map.class.isAssignableFrom(configType)) && (targetType == Guns.Gun.class);
        }
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
