package com.my.world.module.render;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.*;
import com.my.world.core.util.Disposable;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.common.Position;
import com.my.world.module.common.Script;

import java.util.ArrayList;
import java.util.List;

public class RenderSystem extends BaseSystem implements Disposable {

    @Config(type = Config.Type.Asset, elementType = Shader.class)
    public List<Shader> shaders = new ArrayList<>();

    protected final EntityFilter beforeRenderFilter = entity -> entity.contain(BeforeRender.class);
    protected final EntityFilter afterRenderFilter = entity -> entity.contain(AfterRender.class);
    protected ModelBatch batch = new ModelBatch(null, new ShaderProvider() {

        public final ShaderProvider defaultShaderProvider = new DefaultShaderProvider();

        @Override
        public Shader getShader(Renderable renderable) {
            for (Shader shader : shaders) {
                if (shader.canRender(renderable)) return shader;
            }
            return defaultShaderProvider.getShader(renderable);
        }

        @Override
        public void dispose() {
            defaultShaderProvider.dispose();
        }

    }, null);

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
                        if (environment != null && render.includeEnv) {
                            batch.render(render.modelInstance, environment, render.shader);
                        } else {
                            batch.render(render.modelInstance, render.shader);
                        }
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
