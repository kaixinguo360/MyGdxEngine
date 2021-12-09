package com.my.utils.world.sys;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Render;

public class RenderSystem extends BaseSystem {

    // ----- Tmp ----- //
    private static final Vector3 tmp = new Vector3();

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
        for (Entity entity : entities) {
            Position position = entity.getComponent(Position.class);
            Render render = entity.getComponent(Render.class);

            if (isVisible(cam, position, render)) {
                if (environment != null && render.includeEnv)
                    batch.render(render.renderableProvider, environment);
                else
                    batch.render(render.renderableProvider);
            }
        }
        batch.end();
    }
    private boolean isVisible(PerspectiveCamera cam, Position position, Render render) {
        position.getTransform().getTranslation(tmp);
        tmp.add(render.center);
        return cam.frustum.sphereInFrustum(tmp, render.radius);
    }

    @Override
    public void dispose() {
        batch.dispose();
        batch = null;
    }

}
