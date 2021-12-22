package com.my.utils.world.sys;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Render;

public class RenderSystem extends BaseSystem {

    protected ModelBatch batch;

    public RenderSystem() {
        batch = new ModelBatch();
        addDisposable(batch);
    }

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(Position.class, Render.class);
    }

    // ----- Custom ----- //

    public void render(PerspectiveCamera cam, Environment environment) {
        batch.begin(cam);
        for (Entity entity : getEntities()) {
            Position position = entity.getComponent(Position.class);
            Render render = entity.getComponent(Render.class);
            position.getGlobalTransform(render.modelInstance.transform);

            if (isVisible(cam, position, render)) {
                if (environment != null && render.includeEnv)
                    batch.render(render.modelInstance, environment);
                else
                    batch.render(render.modelInstance);
            }
        }
        batch.end();
    }

    // ----- Private ----- //

    private boolean isVisible(PerspectiveCamera cam, Position position, Render render) {
        position.getLocalTransform().getTranslation(tmp);
        tmp.add(render.center);
        return cam.frustum.sphereInFrustum(tmp, render.radius);
    }

    private static final Vector3 tmp = new Vector3();
    private static final BoundingBox boundingBox = new BoundingBox();

    // ----- Inner Class ----- //

    public static class RenderModel {

        public Model model;
        public final Vector3 center = new Vector3();
        public final Vector3 dimensions = new Vector3();
        public float radius;

        public RenderModel(Model model) {
            this.model = model;
            model.calculateBoundingBox(boundingBox);
            boundingBox.getCenter(center);
            boundingBox.getDimensions(dimensions);
            radius = dimensions.len() / 2f;
        }
    }
}
