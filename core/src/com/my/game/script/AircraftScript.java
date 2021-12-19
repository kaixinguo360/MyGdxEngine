package com.my.game.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.game.MyInstance;
import com.my.utils.world.*;
import com.my.utils.world.com.*;
import com.my.utils.world.sys.CameraSystem;
import com.my.utils.world.sys.KeyInputSystem;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.ScriptSystem;

import java.lang.System;
import java.util.HashMap;
import java.util.Map;

public class AircraftScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate, KeyInputSystem.OnKeyDown {

    // ----- Constants ----- //
    private final static short BOMB_FLAG = 1 << 8;
    private final static short AIRCRAFT_FLAG = 1 << 9;
    private final static short ALL_FLAG = -1;

    // ----- Temporary ----- //
    private static final Vector3 tmpV1 = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();
    private static final Quaternion tmpQ = new Quaternion();

    private World world;
    private AssetsManager assetsManager;
    private PhysicsSystem physicsSystem;
    private Camera camera;

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

    public boolean disabled;

    public int bombNum;

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        EntityManager entityManager = context.getEnvironment("world", World.class).getEntityManager();
        body = entityManager.getEntity((String) config.get("body"));
        engine = entityManager.getEntity((String) config.get("engine"));
        rotate_L = entityManager.getEntity((String) config.get("rotate_L"));
        wing_L1 = entityManager.getEntity((String) config.get("wing_L1"));
        wing_L2 = entityManager.getEntity((String) config.get("wing_L2"));
        rotate_R = entityManager.getEntity((String) config.get("rotate_R"));
        wing_R1 = entityManager.getEntity((String) config.get("wing_R1"));
        wing_R2 = entityManager.getEntity((String) config.get("wing_R2"));
        rotate_T = entityManager.getEntity((String) config.get("rotate_T"));
        wing_TL = entityManager.getEntity((String) config.get("wing_TL"));
        wing_TR = entityManager.getEntity((String) config.get("wing_TR"));
        wing_VL = entityManager.getEntity((String) config.get("wing_VL"));
        wing_VR = entityManager.getEntity((String) config.get("wing_VR"));
        disabled = (boolean) config.get("disabled");
        if (rotate_L.contains(AircraftController.class))
            aircraftController_L = rotate_L.getComponent(AircraftController.class);
        if (rotate_R.contains(AircraftController.class))
            aircraftController_R = rotate_R.getComponent(AircraftController.class);
        if (rotate_T.contains(AircraftController.class))
            aircraftController_T = rotate_T.getComponent(AircraftController.class);
        bombNum = (Integer) config.get("bombNum");
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        Map<String, Object> config = new HashMap<>();
        config.put("body", body.getId());
        config.put("engine", engine.getId());
        config.put("rotate_L", rotate_L.getId());
        config.put("wing_L1", wing_L1.getId());
        config.put("wing_L2", wing_L2.getId());
        config.put("rotate_R", rotate_R.getId());
        config.put("wing_R1", wing_R1.getId());
        config.put("wing_R2", wing_R2.getId());
        config.put("rotate_T", rotate_T.getId());
        config.put("wing_TL", wing_TL.getId());
        config.put("wing_TR", wing_TR.getId());
        config.put("wing_VL", wing_VL.getId());
        config.put("wing_VR", wing_VR.getId());
        config.put("bombNum", bombNum);
        config.put("disabled", disabled);
        return config;
    }

    @Override
    public void start(World world, Entity entity) {
        this.world = world;
        this.assetsManager = world.getAssetsManager();
        this.physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
        this.camera = body.getComponent(Camera.class);
    }

    @Override
    public void update(World world, Entity entity) {
        if (camera == null || disabled) return;
        update();
    }

    @Override
    public void keyDown(World world, Entity entity, int keycode) {
        if (camera == null) return;
        if (keycode == Input.Keys.TAB) changeCamera();
        if (keycode == Input.Keys.SHIFT_LEFT && !disabled) changeCameraFollowType();
    }

    public void update() {
        float v1 = 1f;
        float v2 = 0.5f;
        if (aircraftController_L != null && aircraftController_R != null) {
            if (Gdx.input.isKeyPressed(Input.Keys.W)) aircraftController_T.rotate(v1);
            if (Gdx.input.isKeyPressed(Input.Keys.S)) aircraftController_T.rotate(-v1);
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                aircraftController_L.rotate(v2);
                aircraftController_R.rotate(-v2);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                aircraftController_L.rotate(-v2);
                aircraftController_R.rotate(v2);
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
        body.removeComponent(Constraint.class);
        engine.removeComponent(Constraint.class);
        rotate_L.removeComponent(Constraint.class);
        rotate_R.removeComponent(Constraint.class);
        rotate_T.removeComponent(Constraint.class);
        rotate_L.removeComponent(ConstraintController.class);
        rotate_R.removeComponent(ConstraintController.class);
        rotate_T.removeComponent(ConstraintController.class);
        wing_L1.removeComponent(Constraint.class);
        wing_L2.removeComponent(Constraint.class);
        wing_R1.removeComponent(Constraint.class);
        wing_R2.removeComponent(Constraint.class);
        wing_TL.removeComponent(Constraint.class);
        wing_TR.removeComponent(Constraint.class);
        wing_VL.removeComponent(Constraint.class);
        wing_VR.removeComponent(Constraint.class);
        physicsSystem.addExplosion(getTransform().getTranslation(tmpV1), 2000);
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

    public Entity createBomb(Matrix4 transform) {
        Entity entity = new MyInstance(assetsManager, "bomb", null,
                new Collision(BOMB_FLAG, ALL_FLAG));
        entity.setId("Bomb-" + bombNum++);
        world.getEntityManager().addEntity(entity).getComponent(Position.class).transform.set(transform);
        entity.addComponent(new RemoveScript());
        entity.addComponent(new AircraftBombCollisionHandler());
        return entity;
    }

    public void changeCamera() {
        disabled = !disabled;
        if (!disabled) {
            camera.layer = 0;
            camera.startX = 0;
            camera.startY = 0;
            camera.endX = 1;
            camera.endY = 1;
        } else {
            camera.layer = 1;
            camera.startX = 0;
            camera.startY = 0.7f;
            camera.endX = 0.3f;
            camera.endY = 1;
        }
        world.getSystemManager().getSystem(CameraSystem.class).updateCameras();
    }

    public void changeCameraFollowType() {
        switch (camera.followType) {
            case A:
                camera.followType = CameraSystem.FollowType.B;
                break;
            case B:
                camera.followType = CameraSystem.FollowType.A;
                break;
        }
    }
}
