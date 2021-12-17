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
import com.my.utils.world.com.Constraint;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.com.Script;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;

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
                    base == null ? null : new Constraints.ConnectConstraint(base.getId(), id, null, 2000)
            );
        }

        private int rotateNum = 0;
        private Entity createRotate(Matrix4 transform, Constraint.ConstraintController controller, Entity base) {
            Matrix4 relTransform = new Matrix4(base.getComponent(Position.class).transform).inv().mul(transform);
            String id = "GunRotate-" + rotateNum++;
            Entity entity = addObject(
                    id, transform, new MyInstance(assetsManager, "gunRotate", group),
                    base == null ? null : new Constraints.HingeConstraint(
                            base.getId(), id, controller,
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

    public static class Gun implements CameraController, Component, LoadableResource {

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

        @Override
        public void load(Map<String, Object> config, LoadContext context) {
            EntityManager entityManager = context.getEnvironment("world", World.class).getEntityManager();
            rotate_Y = entityManager.getEntity((String) config.get("rotate_Y"));
            rotate_X = entityManager.getEntity((String) config.get("rotate_X"));
            barrel = entityManager.getEntity((String) config.get("barrel"));
            // TODO: Optimize Constraint Component
            if (rotate_Y.contains(Constraint.class)) gunController_Y = (GunController) rotate_Y.getComponents(Constraint.class).get(0).controller;
            if (rotate_X.contains(Constraint.class)) gunController_X = (GunController) rotate_X.getComponents(Constraint.class).get(0).controller;
            bulletNum = (Integer) config.get("bulletNum");
        }

        @Override
        public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
            return new HashMap<String, Object>() {{
                put("rotate_Y", rotate_Y.getId());
                put("rotate_X", rotate_X.getId());
                put("barrel", barrel.getId());
                put("bulletNum", bulletNum);
            }};
        }
    }

    public static class GunScript extends Script {

        private World world;
        private AssetsManager assetsManager;
        private PhysicsSystem physicsSystem;
        private Guns.Gun gun;

        @Override
        public void init(World world, Entity entity) {
            this.world = world;
            this.assetsManager = world.getAssetsManager();
            this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
            this.gun = entity.getComponent(Guns.Gun.class);
        }

        @Override
        public void execute(World world, Entity entity) {
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
            // TODO: Optimize Constraint Component
            gun.rotate_Y.removeComponents(Constraint.class);
            gun.rotate_X.removeComponents(Constraint.class);
            gun.barrel.removeComponents(Constraint.class);
            physicsSystem.addExplosion(getTransform(gun).getTranslation(tmpV), 2000);
        }

        private Entity createBullet(Guns.Gun gun, Matrix4 transform) {
            Entity entity = new MyInstance(assetsManager, "bullet", "bullet", null,
                    new Collisions.BulletCollisionHandler(BOMB_FLAG, ALL_FLAG));
            entity.setId("Bullet-" + gun.bulletNum++);
            world.getEntityManager().addEntity(entity).getComponent(Position.class).transform.set(transform);
            entity.addComponent(new Scripts.RemoveScript());
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

    public static class GunController implements Constraint.ConstraintController, StandaloneResource {

        @Config public float target = 0;
        @Config public float max = 0;
        @Config public float min = 0;
        @Config public boolean limit = false;

        public GunController() {}

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
}
