package com.my.utils.world.com;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.my.utils.world.Component;

import java.util.HashMap;
import java.util.Map;

public class Render implements Component {

    private static final BoundingBox boundingBox = new BoundingBox();

    public RenderableProvider renderableProvider;
    public boolean includeEnv;
    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public float radius;

    // ----- Config ----- //
    public static class Config {
        private Model model;
        private boolean includeEnv;
        private final Vector3 center = new Vector3();
        private final Vector3 dimensions = new Vector3();
        private float radius;


        public Config(Model model) {
            this(model, true);
        }

        public Config(Model model, boolean includeEnv) {
            this.model = model;
            this.includeEnv = includeEnv;
            model.calculateBoundingBox(boundingBox);
            boundingBox.getCenter(center);
            boundingBox.getDimensions(dimensions);
            radius = dimensions.len() / 2f;
        }
    }
    private final static Map<String, Config> configs = new HashMap<>();
    public static void addConfig(String name, Config config) {
        configs.put(name, config);
    }
    public static void removeConfig(String name) {
        configs.remove(name);
    }
    public static Render get(String configName, Position position) {
        if (! configs.containsKey(configName)) return null;

        Config config = configs.get(configName);
        ModelInstance instance = new ModelInstance(config.model);
        position.setTransform(instance.transform);

        Render render = new Render();
        render.renderableProvider = instance;
        render.includeEnv = config.includeEnv;
        render.center.set(config.center);
        render.dimensions.set(config.dimensions);
        render.radius = config.radius;
        return render;
    }
}
