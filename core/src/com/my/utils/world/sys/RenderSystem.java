package com.my.utils.world.sys;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.EntityListener;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Render;
import com.my.utils.world.com.RigidBody;

public class RenderSystem extends BaseSystem implements EntityListener {

    // ----- Tmp ----- //
    private static final Vector3 tmp = new Vector3();
    private static final BoundingBox boundingBox = new BoundingBox();

    protected ModelBatch batch;

    // ----- Create ModelBatch ----- //
    public RenderSystem() {
        batch = new ModelBatch();
    }

    // ----- Check ----- //
    public boolean isHandleable(Entity entity) {
        return entity.contain(Position.class, Render.class);
    }

    // ----- Custom ----- //
    public void render(PerspectiveCamera cam, Environment environment) {
        batch.begin(cam);
        for (Entity entity : getEntities()) {
            Position position = entity.getComponent(Position.class);
            Render render = entity.getComponent(Render.class);

            if (isVisible(cam, position, render)) {
                if (environment != null && render.includeEnv)
                    batch.render(render.modelInstance, environment);
                else
                    batch.render(render.modelInstance);
            }
        }
        batch.end();
    }
    private boolean isVisible(PerspectiveCamera cam, Position position, Render render) {
        position.transform.getTranslation(tmp);
        tmp.add(render.center);
        return cam.frustum.sphereInFrustum(tmp, render.radius);
    }

    @Override
    public void dispose() {
        batch.dispose();
        batch = null;
    }

    @Override
    public void afterAdded(Entity entity) {
        Position position = entity.getComponent(Position.class);
        Render render = entity.getComponent(Render.class);
        render.modelInstance.transform.set(position.transform);
        position.transform = render.modelInstance.transform;
        if (entity.contain(RigidBody.class)) {
            entity.getComponent(RigidBody.class).body.proceedToTransform(position.transform);
        }
    }

    @Override
    public void afterRemoved(Entity entity) {

    }

    // ----- Config ----- //
    public static class RenderConfig {
        private Model model;
        private boolean includeEnv;
        private final Vector3 center = new Vector3();
        private final Vector3 dimensions = new Vector3();
        private float radius;


        public RenderConfig(Model model) {
            this(model, true);
        }

        public RenderConfig(Model model, boolean includeEnv) {
            this.model = model;
            this.includeEnv = includeEnv;
            model.calculateBoundingBox(boundingBox);
            boundingBox.getCenter(center);
            boundingBox.getDimensions(dimensions);
            radius = dimensions.len() / 2f;
        }

        public Render newInstance() {
            Render render = new Render();
            render.renderConfig = this;
            render.modelInstance = new ModelInstance(this.model);
            render.includeEnv = this.includeEnv;
            render.center.set(this.center);
            render.dimensions.set(this.dimensions);
            render.radius = this.radius;
            return render;
        }
    }
}
