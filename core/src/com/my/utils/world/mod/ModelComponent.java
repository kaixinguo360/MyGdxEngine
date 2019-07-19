package com.my.utils.world.mod;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Pool;
import com.my.utils.world.Component;

import java.util.HashMap;
import java.util.Map;

public class ModelComponent implements Component, Pool.Poolable {

    // ----- Static ----- //
    public static final Pool<ModelComponent> pool = new Pool<ModelComponent>() {
        @Override
        protected ModelComponent newObject() {
            return new ModelComponent();
        }
    };
    private static final BoundingBox bounds = new BoundingBox();
    private static final Vector3 tmp = new Vector3();

    // ----- Properties ----- //
    ModelInstance instance;
    boolean includeEnv = true;
    final Vector3 center = new Vector3();
    final Vector3 dimensions = new Vector3();
    float radius = 0;

    // ----- Set & Reset ----- //
    public ModelComponent set(Config config) {
        this.instance = new ModelInstance(config.model);
        this.includeEnv = config.includeEnv;
        this.center.set(config.center);
        this.dimensions.set(config.dimensions);
        this.radius = config.radius;
        return this;
    }
    public ModelComponent set(ModelInstance instance, boolean includeEnv) {
        this.instance = instance;
        this.includeEnv = includeEnv;
        instance.calculateBoundingBox(bounds);
        bounds.getCenter(center);
        bounds.getDimensions(dimensions);
        radius = dimensions.len() / 2f;
        return this;
    }
    public void reset() {
        instance = null;
        includeEnv = true;
        this.center.setZero();
        this.dimensions.setZero();
        this.radius = 0;
    }

    // ----- Custom ----- //
    boolean isVisible(PerspectiveCamera cam) {
        instance.transform.getTranslation(tmp);
        tmp.add(center);
        return cam.frustum.sphereInFrustum(tmp, radius);
    }
    public Matrix4 getTransform() {
        return instance.transform;
    }
    public ModelInstance getModelInstance() {
        return instance;
    }

    // ----- Config ----- //
    public static class Config {

        private final Model model;
        private final boolean includeEnv;
        private final Vector3 center = new Vector3();
        private final Vector3 dimensions = new Vector3();
        private float radius;

        public Config(Model model) {
            this(model, true);
        }
        public Config(Model model, boolean includeEnv) {
            this.model = model;
            this.includeEnv = includeEnv;
            model.calculateBoundingBox(bounds);
            bounds.getCenter(center);
            bounds.getDimensions(dimensions);
            radius = dimensions.len() / 2f;
        }
    }
    private final static Map<String, Config> configs = new HashMap<>();
    public static void addConfig(String name, ModelComponent.Config config) {
        configs.put(name, config);
    }
    public static void removeConfig(String name) {
        configs.remove(name);
    }
    public static ModelComponent obtainComponent(String configName) {
        Config config = configs.get(configName);
        if (config != null)
            return pool.obtain().set(config);
        else
            return null;
    }
}
