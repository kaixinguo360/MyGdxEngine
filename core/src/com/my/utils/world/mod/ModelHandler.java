package com.my.utils.world.mod;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.world.Handler;
import com.my.utils.world.Module;

import java.util.HashMap;
import java.util.Map;

public class ModelHandler implements Handler, Disposable {

    // Tmp Vector
    private static final Vector3 tmpPosition = new Vector3();

    protected ModelBatch batch;

    // ----- Create batch ----- //
    public ModelHandler() {
        batch = new ModelBatch();
    }

    // ----- Module ----- //
    private final Map<ModelModule, String> modules = new HashMap<>();
    public void add(Module module, String instanceName) {
        if (!(module instanceof ModelModule)) throw new IllegalArgumentException();
        modules.put((ModelModule) module, instanceName);
    }
    public void remove(Module module) {
        if (!(module instanceof ModelModule)) throw new IllegalArgumentException();
        modules.remove(module);
    }
    public boolean handle(Module module) {
        return module instanceof ModelModule;
    }
    public ModelModule findModule(ModelComponent component) {
        for (ModelModule module : modules.keySet()) {
            if (module.contain(component)) {
                return module;
            }
        }
        return null;
    }

    // ----- Custom ----- //

    // Render Scene
    public void render(PerspectiveCamera cam, Environment environment) {
        batch.begin(cam);
        for (ModelModule modelModule : modules.keySet()) {
            for (ModelComponent component : modelModule.getAll()) {
                if (component.isVisible(cam)) {
                    if (environment != null && component.includeEnv)
                        batch.render(component.instance, environment);
                    else
                        batch.render(component.instance);
                }
            }
        }
        batch.end();
    }
    // Get Instance Name From PickRay
    public String pick(Camera cam, int X, int Y) {
        Ray ray = cam.getPickRay(X, Y);

        ModelModule result = null;
        float distance = -1;

        for (ModelModule module : modules.keySet()) {
            for (ModelComponent component : module.getAll()) {

                ModelInstance instance = component.getModelInstance();
                instance.transform.getTranslation(tmpPosition);
                tmpPosition.add(component.center);

                float dist2 = ray.origin.dst2(tmpPosition);
                if (result != null && dist2 > distance) {
                    continue;
                }
                if (Intersector.intersectRaySphere(ray, tmpPosition, component.radius, null)) {
                    result = module;
                    distance = dist2;
                }

            }
        }
        return modules.get(result);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
