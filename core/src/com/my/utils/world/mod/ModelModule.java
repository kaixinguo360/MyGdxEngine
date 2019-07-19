package com.my.utils.world.mod;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.my.utils.world.BaseModule;
import com.my.utils.world.Component;

public class ModelModule extends BaseModule<ModelComponent> {

    // Tmp Vector
    private static final Vector3 tmp = new Vector3();

    protected ModelBatch batch;

    // ----- Create batch ----- //
    public ModelModule() {
        batch = new ModelBatch();
        addDisposable(batch);
    }

    // ----- Component ----- //
    public boolean handle(Component component) {
        return component instanceof ModelComponent;
    }

    // ----- Custom ----- //

    // Render Scene
    public void render(PerspectiveCamera cam, Environment environment) {
        batch.begin(cam);
        for (ModelComponent component : components.keySet()) {
            if (component.isVisible(cam)) {
                if (environment != null && component.includeEnv)
                    batch.render(component.instance, environment);
                else
                    batch.render(component.instance);
            }
        }
        batch.end();
    }
    // Get Instance Name From PickRay
    public String pick(Camera cam, int X, int Y) {
        Ray ray = cam.getPickRay(X, Y);

        ModelComponent result = null;
        float distance = -1;

        for (ModelComponent component : components.keySet()) {
            {

                ModelInstance instance = component.getModelInstance();
                instance.transform.getTranslation(tmp);
                tmp.add(component.center);

                float dist2 = ray.origin.dst2(tmp);
                if (result != null && dist2 > distance) {
                    continue;
                }
                if (Intersector.intersectRaySphere(ray, tmp, component.radius, null)) {
                    result = component;
                    distance = dist2;
                }

            }
        }
        return components.get(result);
    }
}
