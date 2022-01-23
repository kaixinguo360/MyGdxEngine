package com.my.world.module.render;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.System;
import com.my.world.core.util.Disposable;
import com.my.world.gdx.Vector3Pool;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.common.Position;
import com.my.world.module.common.RenderSystem;

import java.util.ArrayList;
import java.util.List;

public class DefaultRenderSystem extends BaseSystem implements System.OnStart, RenderSystem, Disposable {

    @Config(type = Config.Type.Asset, elementType = Shader.class)
    public List<Shader> shaders = new ArrayList<>();

    protected EnvironmentSystem environmentSystem;
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
    public void start(Scene scene) {
        List<EnvironmentSystem> systemList = scene.getSystemManager().getSystems(EnvironmentSystem.class);
        environmentSystem = systemList.get(0);
    }

    @Override
    public void dispose() {
        batch.dispose();
        environmentSystem = null;
    }

    // ----- RenderSystem ----- //

    protected Environment currentEnvironment;

    @Override
    public void begin() {
        currentEnvironment = environmentSystem.getEnvironment();
    }

    @Override
    public void render(PerspectiveCamera cam) {
        batch.begin(cam);
        for (Entity entity : getEntities()) {
            Position position = entity.getComponent(Position.class);
            for (Render render : entity.getComponents(Render.class)) {
                if (render.isActive()) {
                    render.modelInstance.transform.set(position.getGlobalTransform());

                    if (isVisible(cam, position, render)) {
                        if (currentEnvironment != null && render.includeEnv) {
                            batch.render(render.modelInstance, currentEnvironment, render.shader);
                        } else {
                            batch.render(render.modelInstance, render.shader);
                        }
                    }
                }
            }
        }
        batch.end();
    }

    @Override
    public void end() {
        currentEnvironment = null;
    }

    // ----- Protected ----- //

    protected boolean isVisible(PerspectiveCamera cam, Position position, Render render) {
        Vector3 tmpV = Vector3Pool.obtain();
        position.getGlobalTransform().getTranslation(tmpV);
        tmpV.add(render.center);
        boolean b = cam.frustum.sphereInFrustum(tmpV, render.radius);
        Vector3Pool.free(tmpV);
        return b;
    }
}
