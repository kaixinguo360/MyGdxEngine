package com.my.utils.world.com;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.Component;

import java.util.HashMap;
import java.util.Map;

public class RigidBody implements Component, Disposable {

    public btRigidBody body;
    public int group;
    public int mask;

    @Override
    public void dispose() {
        if (body != null) body.dispose();
    }

    // ----- Static ----- //
    public final static short STATIC_FLAG = 1 << 8;
    public final static short NORMAL_FLAG = 1 << 9;
    public final static short ALL_FLAG = -1;

    // ----- Config ----- //
    public static class Config {

        private static final Vector3 localInertia = new Vector3();

        private final btRigidBody.btRigidBodyConstructionInfo constructionInfo;
        private final int group;
        private final int mask;

        public Config(btCollisionShape shape, float mass) {
            this(shape, mass, NORMAL_FLAG, ALL_FLAG);
        }

        public Config(btCollisionShape shape, float mass, int group, int mask) {
            this.group = group;
            this.mask = mask;
            if (mass > 0f)
                shape.calculateLocalInertia(mass, localInertia);
            else
                localInertia.set(0, 0, 0);
            this.constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
        }
    }
    private final static Map<String, Config> configs = new HashMap<>();
    public static void addConfig(String name, Config config) {
        configs.put(name, config);
    }
    public static void removeConfig(String name) {
        configs.remove(name);
    }
    public static RigidBody get(String configName) {
        if (! configs.containsKey(configName)) return null;
        Config config = configs.get(configName);
        RigidBody rigidBody = new RigidBody();
        rigidBody.body = new btRigidBody(config.constructionInfo);
        rigidBody.group = config.group;
        rigidBody.mask = config.mask;
        return rigidBody;
    }
}
