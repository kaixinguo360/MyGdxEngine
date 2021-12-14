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
import com.my.utils.world.com.*;
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

        private static final String group = "group";
        // ----- Variables ----- //
        private World world;
        private AssetsManager assetsManager;

        // ----- Init ----- //
        public GunBuilder(World world) {
            this.world = world;
            this.assetsManager = world.getAssetsManager();
        }

        // ----- Builder Methods ----- //

        private int barrelNum = 0;
        private Entity createBarrel(Matrix4 transform, Entity base) {
            String id = "Barrel-" + barrelNum++;
            return addObject(
                    id, transform, new MyInstance(assetsManager, "barrel", group),
                    base == null ? null : Constraints.ConnectConstraint.getConfig(assetsManager, base.getId(), id, null, 2000)
            );
        }

        private int rotateNum = 0;
        private Entity createRotate(Matrix4 transform, ConstraintSystem.ConstraintController controller, Entity base) {
            Matrix4 relTransform = new Matrix4(base.getComponent(Position.class).transform).inv().mul(transform);
            String id = "GunRotate-" + rotateNum++;
            Entity entity = addObject(
                    id, transform, new MyInstance(assetsManager, "gunRotate", group),
                    base == null ? null : Constraints.HingeConstraint.getConfig(
                            assetsManager, base.getId(), id, controller,
                            relTransform.rotate(Vector3.X, 90),
                            new Matrix4().rotate(Vector3.X, 90),
                            false)
            );
            return entity;
        }

        private int gunNum = 0;
        public Entity createGun(String baseObjectId, Matrix4 transform) {

            // Gun
            Guns.Gun gun = new Guns.Gun();

            gun.gunController_Y = new GunController();
            gun.gunController_X = new GunController(-90, 0);
            gun.rotate_Y = createRotate(transform.cpy().translate(0, 0.5f, 0), gun.gunController_Y, world.getEntityManager().getEntity(baseObjectId));
            gun.rotate_X = createRotate(transform.cpy().translate(0, 1.5f, 0).rotate(Vector3.Z, 90), gun.gunController_X, gun.rotate_Y);
            gun.barrel = createBarrel(transform.cpy().translate(0, 1.5f, -3), gun.rotate_X);

            // Gun Entity
            Entity entity = new Entity();
            entity.setId("Gun-" + gunNum++);
            entity.addComponent(gun);
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

    public static class Gun implements CameraController, Component {

        // ----- Temporary ----- //
        private static final Vector3 tmpV = new Vector3();
        private static final Matrix4 tmpM = new Matrix4();
        private static final Quaternion tmpQ = new Quaternion();

        private Entity rotate_Y, rotate_X, barrel;
        private GunController gunController_Y;
        private GunController gunController_X;

        int bulletNum;

        @Override
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

    public static class GunScript implements ScriptSystem.Script, AfterAdded {

        private World world;
        private AssetsManager assetsManager;
        private PhysicsSystem physicsSystem;

        @Override
        public void afterAdded(World world) {
            this.world = world;
            this.assetsManager = world.getAssetsManager();
            this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
        }

        @Override
        public void init(World world, Entity entity, ScriptComponent scriptComponent) {
            scriptComponent.customObj = entity.getComponent(Guns.Gun.class);
            Guns.Gun gun = entity.getComponent(Guns.Gun.class);
        }

        @Override
        public void execute(World world, Entity entity, ScriptComponent scriptComponent) {
            Guns.Gun gun = (Guns.Gun) scriptComponent.customObj;
            update(gun);
        }

        // ----- Constants ----- //
        private final static short BOMB_FLAG = 1 << 8;
        private final static short GUN_FLAG = 1 << 9;
        private final static short ALL_FLAG = -1;

        // ----- Temporary ----- //
        private static final Vector3 tmpV = new Vector3();
        private static final Matrix4 tmpM = new Matrix4();
        private static final Quaternion tmpQ = new Quaternion();

        public void update(Guns.Gun gun) {
            float v = 0.025f;
            if (gun.gunController_Y != null && gun.gunController_X != null) {
                if (Gdx.input.isKeyPressed(Input.Keys.W)) rotate(gun, 0, -v);
                if (Gdx.input.isKeyPressed(Input.Keys.S)) rotate(gun, 0, v);
                if (Gdx.input.isKeyPressed(Input.Keys.A)) rotate(gun, v, 0);
                if (Gdx.input.isKeyPressed(Input.Keys.D)) rotate(gun, -v, 0);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.J)) fire(gun);
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) explode(gun);
        }
        public void fire(Guns.Gun gun) {
            tmpM.set(getTransform(gun)).translate(0, 0, -20 + (float) (Math.random() * 15)).rotate(Vector3.X, 90);
            getTransform(gun).getRotation(tmpQ);
            tmpV.set(getBody(gun).getLinearVelocity());
            tmpV.add(new Vector3(0, 0, -1).mul(tmpQ).scl(2000));
            btRigidBody body = createBullet(gun, tmpM).getComponent(RigidBody.class).body;
            body.setLinearVelocity(tmpV);
            body.setCcdMotionThreshold(1e-7f);
            body.setCcdSweptSphereRadius(2);
        }
        public void explode(Guns.Gun gun) {
            System.out.println("Explosion!");
            gun.rotate_Y.removeComponent(Constraint.class);
            gun.rotate_X.removeComponent(Constraint.class);
            gun.barrel.removeComponent(Constraint.class);
            physicsSystem.addExplosion(getTransform(gun).getTranslation(tmpV), 2000);
        }

        private Entity createBullet(Guns.Gun gun, Matrix4 transform) {
            Entity entity = new MyInstance(assetsManager, "bullet", "bullet", null,
                    new Collision(BOMB_FLAG, ALL_FLAG, assetsManager.getAsset("BulletCollisionHandler", PhysicsSystem.CollisionHandler.class)));
            entity.setId("Bullet-" + gun.bulletNum++);
            world.getEntityManager().addEntity(entity).getComponent(Position.class).transform.set(transform);
            ScriptComponent scriptComponent = new ScriptComponent();
            scriptComponent.script = assetsManager.getAsset("RemoveScript", ScriptSystem.Script.class);
            entity.addComponent(scriptComponent);
            return entity;
        }
        public void rotate(Guns.Gun gun, float stepY, float stepX) {
            setDirection(gun, gun.gunController_Y.target + stepY, gun.gunController_X.target + stepX);
        }
        public void setDirection(Guns.Gun gun, float angleY, float angleX) {
            getBody(gun).activate();
            gun.gunController_Y.target = angleY;
            gun.gunController_X.target = angleX;
        }
        public Matrix4 getTransform(Guns.Gun gun) {
            return gun.barrel.getComponent(Position.class).transform;
        }
        public btRigidBody getBody(Guns.Gun gun) {
            return gun.barrel.getComponent(RigidBody.class).body;
        }
    }

    public static class GunLoader implements Loader {

        @Override
        public <E, T> T load(E config, Class<T> type, LoadContext context) {
            EntityManager entityManager = context.getEnvironment("world", World.class).getEntityManager();
            Map<String, Object> map = (Map<String, Object>) config;
            Guns.Gun gun = new Guns.Gun();
            gun.rotate_Y = entityManager.getEntity((String) map.get("rotate_Y"));
            gun.rotate_X = entityManager.getEntity((String) map.get("rotate_X"));
            gun.barrel = entityManager.getEntity((String) map.get("barrel"));
            if (gun.rotate_Y.contain(Constraint.class)) gun.gunController_Y = (GunController) gun.rotate_Y.getComponent(Constraint.class).controller;
            if (gun.rotate_X.contain(Constraint.class)) gun.gunController_X = (GunController) gun.rotate_X.getComponent(Constraint.class).controller;
            gun.bulletNum = (Integer) map.get("bulletNum");
            return (T) gun;
        }

        @Override
        public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
            Guns.Gun gun = (Guns.Gun) obj;
            return (E) new HashMap<String, Object>() {{
                put("rotate_Y", gun.rotate_Y.getId());
                put("rotate_X", gun.rotate_X.getId());
                put("barrel", gun.barrel.getId());
                put("bulletNum", gun.bulletNum);
            }};
        }

        @Override
        public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
            return (Map.class.isAssignableFrom(configType)) && (targetType == Guns.Gun.class);
        }
    }

    private static class GunController implements ConstraintSystem.ConstraintController {
        private float target = 0;
        private float max = 0;
        private float min = 0;
        private boolean limit = false;
        private GunController() {}
        private GunController(float min, float max) {
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

    public static class GunControllerLoader implements Loader {

        @Override
        public <E, T> T load(E config, Class<T> type, LoadContext context) {
            Map<String, Object> map = (Map<String, Object>) config;
            Guns.GunController controller = new Guns.GunController(
                    (float) (double) map.get("min"),
                    (float) (double) map.get("max")
            );
            controller.target = (float) (double) map.get("target");
            controller.limit = (Boolean) map.get("limit");
            return (T) controller;
        }

        @Override
        public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
            Guns.GunController controller = (Guns.GunController) obj;
            return (E) new HashMap<String, Object>(){{
                put("min", (double) controller.min);
                put("max", (double) controller.max);
                put("target", (double) controller.target);
                put("limit", controller.limit);
            }};
        }

        @Override
        public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
            return (Map.class.isAssignableFrom(configType)) && (targetType == Guns.GunController.class);
        }
    }
}
