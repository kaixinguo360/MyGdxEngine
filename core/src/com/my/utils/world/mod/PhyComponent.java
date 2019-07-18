package com.my.utils.world.mod;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Pool;

import java.util.HashMap;
import java.util.Map;

public class PhyComponent implements Pool.Poolable {

    // ----- Static ----- //
    public final static short STATIC_FLAG = 1 << 8;
    public final static short NORMAL_FLAG = 1 << 9;
    public final static short ALL_FLAG = -1;
    public static final Pool<PhyComponent> pool = new Pool<PhyComponent>() {
        @Override
        protected PhyComponent newObject() {
            return new PhyComponent();
        }
    };

    // ----- Properties ----- //
    private MotionState motionState = new MotionState();
    btRigidBody body;
    int group;
    int mask;

    // ----- Set & Reset ----- //
    public PhyComponent set(Config config) {
        body = new btRigidBody(config.constructionInfo);
        body.userData = this;
        group = config.group;
        mask = config.mask;
        return this;
    }
    public void reset() {
        motionState.transform = null;
        body.setMotionState(null);
        body.userData = null;
        body.dispose();
        body = null;
        group = 0;
        mask = 0;
    }

    // ----- Custom ----- //
    public void setMotionState(Matrix4 transform) {
        body.setMotionState(motionState);
        motionState.transform = transform;
    }
    public void proceedToTransform(Matrix4 transform) {
        body.proceedToTransform(transform);
    }

    // ----- MotionState ----- //
    class MotionState extends btMotionState {
        Matrix4 transform;
        @Override
        public void getWorldTransform (Matrix4 worldTrans) {
            if (transform != null) worldTrans.set(transform);
        }
        @Override
        public void setWorldTransform (Matrix4 worldTrans) {
            if (transform != null) transform.set(worldTrans);
        }
    }

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
    public static void addConfig(String name, PhyComponent.Config config) {
        configs.put(name, config);
    }
    public static void removeConfig(String name) {
        configs.remove(name);
    }
    public static PhyComponent obtainComponent(String configName) {
        Config config = configs.get(configName);
        if (config != null)
            return pool.obtain().set(config);
        else
            return null;
    }
}
