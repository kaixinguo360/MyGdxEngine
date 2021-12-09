package com.my.game;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.World;
import com.my.utils.world.com.Motion;
import com.my.utils.world.com.Position;
import com.my.utils.world.sys.MotionSystem;

import java.util.HashMap;
import java.util.Map;

public class Motions {

    private static final Vector3 TMP_1 = new Vector3();
    private static final Vector3 TMP_2 = new Vector3();

    public static void init(World world) {
        AssetsManager assetsManager = world.getAssetsManager();
        assetsManager.addAsset("Force", MotionSystem.MotionHandler.class, new Force());
        assetsManager.addAsset("FixedForce", MotionSystem.MotionHandler.class, new FixedForce());
        assetsManager.addAsset("LimitedForce", MotionSystem.MotionHandler.class, new LimitedForce());
        assetsManager.addAsset("Lift", MotionSystem.MotionHandler.class, new Lift());
    }

    public static class Force implements MotionSystem.MotionHandler {
        @Override
        public void update(Map<String, Object> config, btRigidBody body, Position position) {
            Vector3 force = (Vector3) config.get("force");
            Vector3 rel_pos = (Vector3) config.get("rel_pos");
            body.applyForce(TMP_1.set(force).rot(position.getTransform()), rel_pos);
        }
        public static Motion getConfig(Vector3 force, Vector3 rel_pos) {
            return new Motion("Force", new HashMap<String, Object>() {{
                put("force", force);
                put("rel_pos", rel_pos);
            }});
        }
    }
    public static class FixedForce implements MotionSystem.MotionHandler {
        @Override
        public void update(Map<String, Object> config, btRigidBody body, Position position) {
            Vector3 force = (Vector3) config.get("force");
            Vector3 rel_pos = (Vector3) config.get("rel_pos");
            body.applyForce(force, rel_pos);
        }
        public static Motion getConfig(Vector3 force, Vector3 rel_pos) {
            return new Motion("FixedForce", new HashMap<String, Object>() {{
                put("force", force);
                put("rel_pos", rel_pos);
            }});
        }
    }
    public static class LimitedForce implements MotionSystem.MotionHandler {
        @Override
        public void update(Map<String, Object> config, btRigidBody body, Position position) {
            float maxVelocity = (float) config.get("maxVelocity");
            Vector3 force = (Vector3) config.get("force");
            Vector3 rel_pos = (Vector3) config.get("rel_pos");
            TMP_1.set(force).rot(position.getTransform()).nor();
            if (Math.abs(body.getLinearVelocity().dot(TMP_1)) <= maxVelocity) {
                body.applyForce(TMP_1.set(force).rot(position.getTransform()), rel_pos);
            }
        }
        public static Motion getConfig(float maxVelocity, Vector3 force, Vector3 rel_pos) {
            return new Motion("LimitedForce", new HashMap<String, Object>() {{
                put("maxVelocity", maxVelocity);
                put("force", force);
                put("rel_pos", rel_pos);
            }});
        }
    }
    public static class Lift implements MotionSystem.MotionHandler {
        @Override
        public void update(Map<String, Object> config, btRigidBody body, Position position) {
            Vector3 up = (Vector3) config.get("up");
            TMP_1.set(body.getLinearVelocity());
            TMP_2.set(up).rot(position.getTransform());
            float lift = - TMP_2.dot(TMP_1);
            TMP_1.set(up).nor().scl(lift).rot(position.getTransform());
            TMP_2.set(0, 0, 0);
            body.applyForce(TMP_1, TMP_2);
        }
        public static Motion getConfig(Vector3 up) {
            return new Motion("Lift", new HashMap<String, Object>() {{
                put("type", "Lift");
                put("up", up);
            }});
        }
    }
}
