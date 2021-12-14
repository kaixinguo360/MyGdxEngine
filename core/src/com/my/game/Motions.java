package com.my.game;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.LoadContext;
import com.my.utils.world.com.Motion;
import com.my.utils.world.com.Position;
import com.my.utils.world.sys.MotionSystem;

import java.util.HashMap;
import java.util.Map;

public class Motions {

    private static final Vector3 TMP_1 = new Vector3();
    private static final Vector3 TMP_2 = new Vector3();

    public static void initAssets(AssetsManager assetsManager) {
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
            body.applyForce(TMP_1.set(force).rot(position.transform), rel_pos);
        }
        public static Motion getConfig(AssetsManager assetsManager, Vector3 force, Vector3 rel_pos) {
            return new Motion(assetsManager.getAsset("Force", MotionSystem.MotionHandler.class), new HashMap<String, Object>() {{
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
        public static Motion getConfig(AssetsManager assetsManager, Vector3 force, Vector3 rel_pos) {
            return new Motion(assetsManager.getAsset("FixedForce", MotionSystem.MotionHandler.class), new HashMap<String, Object>() {{
                put("force", force);
                put("rel_pos", rel_pos);
            }});
        }
    }
    public static class LimitedForce implements MotionSystem.MotionHandler {
        @Override
        public void update(Map<String, Object> config, btRigidBody body, Position position) {
            float maxVelocity = (float) (double) config.get("maxVelocity");
            Vector3 force = (Vector3) config.get("force");
            Vector3 rel_pos = (Vector3) config.get("rel_pos");
            TMP_1.set(force).rot(position.transform).nor();
            if (Math.abs(body.getLinearVelocity().dot(TMP_1)) <= maxVelocity) {
                body.applyForce(TMP_1.set(force).rot(position.transform), rel_pos);
            }
        }
        public static Motion getConfig(AssetsManager assetsManager, float maxVelocity, Vector3 force, Vector3 rel_pos) {
            return new Motion(assetsManager.getAsset("LimitedForce", MotionSystem.MotionHandler.class), new HashMap<String, Object>() {{
                put("maxVelocity", (double) maxVelocity);
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
            TMP_2.set(up).rot(position.transform);
            float lift = - TMP_2.dot(TMP_1);
            TMP_1.set(up).nor().scl(lift).rot(position.transform);
            TMP_2.set(0, 0, 0);
            body.applyForce(TMP_1, TMP_2);
        }
        public static Motion getConfig(AssetsManager assetsManager, Vector3 up) {
            return new Motion(assetsManager.getAsset("Lift", MotionSystem.MotionHandler.class), new HashMap<String, Object>() {{
                put("type", "Lift");
                put("up", up);
            }});
        }
    }

    public static class Loader implements com.my.utils.world.Loader {

        @Override
        public <E, T> T load(E config, Class<T> type, LoadContext context) {
            try {
                return (T) Class.forName(((Map<String, String>) config).get("type")).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException("MotionHandler create error: " + e.getMessage(), e);
            }
        }

        @Override
        public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
            return (E) new HashMap<String, String>() {{
                put("type", obj.getClass().getName());
            }};
        }

        @Override
        public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
            return (Map.class.isAssignableFrom(configType)) && (MotionSystem.MotionHandler.class.isAssignableFrom(targetType));
        }
    }
}
