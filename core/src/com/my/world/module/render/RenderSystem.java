package com.my.world.module.render;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Entity;
import com.my.world.core.util.Disposable;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.common.Position;

public class RenderSystem extends BaseSystem implements Disposable {

    protected final ModelBatch batch  = new ModelBatch();

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(Position.class, Render.class);
    }

    @Override
    public void dispose() {
        batch.dispose();
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
        Vector3 tmp = Vector3Pool.obtain();
        position.getLocalTransform().getTranslation(tmp);
        tmp.add(render.center);
        boolean b = cam.frustum.sphereInFrustum(tmp, render.radius);
        Vector3Pool.free(tmp);
        return b;
    }
}
