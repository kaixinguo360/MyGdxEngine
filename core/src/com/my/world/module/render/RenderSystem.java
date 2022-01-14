package com.my.world.module.render;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Component;
import com.my.world.core.Entity;
import com.my.world.core.EntityFilter;
import com.my.world.core.Scene;
import com.my.world.core.util.Disposable;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.common.Position;
import com.my.world.module.common.Script;

public class RenderSystem extends BaseSystem implements Disposable {

    protected final EntityFilter beforeRenderFilter = entity -> entity.contain(BeforeRender.class);
    protected final EntityFilter afterRenderFilter = entity -> entity.contain(AfterRender.class);
    protected final ModelBatch batch  = new ModelBatch();

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(Position.class, Render.class);
    }

    @Override
    public void afterAdded(Scene scene) {
        super.afterAdded(scene);
        scene.getEntityManager().addFilter(beforeRenderFilter);
        scene.getEntityManager().addFilter(afterRenderFilter);
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    // ----- Custom ----- //

    public void render(PerspectiveCamera cam, Environment environment) {
        for (Entity entity : scene.getEntityManager().getEntitiesByFilter(beforeRenderFilter)) {
            for (BeforeRender script : entity.getComponents(BeforeRender.class)) {
                if (Component.isActive(script)) {
                    script.beforeRender(cam, environment);
                }
            }
        }

        batch.begin(cam);
        for (Entity entity : getEntities()) {
            Position position = entity.getComponent(Position.class);
            for (Render render : entity.getComponents(Render.class)) {
                if (render.isActive()) {
                    render.modelInstance.transform.set(position.getGlobalTransform());

                    if (isVisible(cam, position, render)) {
                        if (environment != null && render.includeEnv)
                            batch.render(render.modelInstance, environment);
                        else
                            batch.render(render.modelInstance);
                    }
                }
            }
        }
        batch.end();

        for (Entity entity : scene.getEntityManager().getEntitiesByFilter(afterRenderFilter)) {
            for (AfterRender script : entity.getComponents(AfterRender.class)) {
                if (Component.isActive(script)) {
                    script.afterRender(cam, environment);
                }
            }
        }
    }

    // ----- Private ----- //

    private boolean isVisible(PerspectiveCamera cam, Position position, Render render) {
        Vector3 tmpV = Vector3Pool.obtain();
        position.getGlobalTransform().getTranslation(tmpV);
        tmpV.add(render.center);
        boolean b = cam.frustum.sphereInFrustum(tmpV, render.radius);
        Vector3Pool.free(tmpV);
        return b;
    }

    public interface BeforeRender extends Script {
        void beforeRender(PerspectiveCamera cam, Environment environment);
    }

    public interface AfterRender extends Script {
        void afterRender(PerspectiveCamera cam, Environment environment);
    }
}
