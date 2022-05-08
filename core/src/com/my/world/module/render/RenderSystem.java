package com.my.world.module.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultRenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.System;
import com.my.world.core.util.Disposable;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.common.Position;

import java.util.ArrayList;
import java.util.List;

public class RenderSystem extends BaseSystem implements System.OnStart, Disposable, System {

    @Config(type = Config.Type.Asset, elementType = Shader.class)
    public List<Shader> shaders = new ArrayList<>();

    @Config(type = Config.Type.Asset)
    public ShaderProvider shaderProvider;

    @Config(type = Config.Type.Asset)
    public RenderableSorter renderableSorter;

    protected Camera currentCamera;
    protected EnvironmentSystem environmentSystem;
    protected ModelBatch batch = new ModelBatch(new ShaderProvider() {

        public final ShaderProvider defaultShaderProvider = new DefaultShaderProvider();

        @Override
        public Shader getShader(Renderable renderable) {
            for (Shader shader : shaders) {
                if (shader.canRender(renderable)) return shader;
            }
            if (shaderProvider != null) {
                return shaderProvider.getShader(renderable);
            } else {
                return defaultShaderProvider.getShader(renderable);
            }
        }

        @Override
        public void dispose() {
            defaultShaderProvider.dispose();
        }

    }, new RenderableSorter() {

        public final DefaultRenderableSorter defaultRenderableSorter = new DefaultRenderableSorter();

        @Override
        public void sort(Camera camera, Array<Renderable> renderables) {
            if (renderableSorter != null) {
                renderableSorter.sort(camera, renderables);
            } else {
                defaultRenderableSorter.sort(camera, renderables);
            }
        }
    });

    @Override
    public boolean canHandle(Entity entity) {
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

    public void begin() {
        currentEnvironment = environmentSystem.getEnvironment();
    }

    public void render(PerspectiveCamera cam) {
        beginBatch(cam);
        for (Entity entity : getEntities()) {
            Position position = entity.getComponent(Position.class);
            for (Render render : entity.getComponents(Render.class)) {
                if (render.isActive()) {
                    render.setTransform(position);
                    addToBatch(render);
                }
            }
        }
        endBatch();
    }

    public void end() {
        currentEnvironment = null;
    }

    // ----- RenderBatch ----- //

    public void beginBatch(Camera cam) {
        if (currentCamera != null) throw new RuntimeException("Current camera is not null");
        currentCamera = cam;
        batch.begin(currentCamera);
    }

    public void endBatch() {
        if (currentCamera == null) throw new RuntimeException("Current camera is null");
        batch.end();
        currentCamera = null;
    }

    public void addToBatch(Render render) {
        if (currentCamera == null) throw new RuntimeException("Current camera is null");
        if (!render.isVisible(currentCamera)) {
            return;
        }
        if (currentEnvironment != null && render.includeEnv) {
            if (render.shader != null) {
                batch.render(render, currentEnvironment, render.shader);
            } else {
                batch.render(render, currentEnvironment);
            }
        } else {
            if (render.shader != null) {
                batch.render(render, render.shader);
            } else {
                batch.render(render);
            }
        }
    }
}
