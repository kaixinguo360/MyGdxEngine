package com.my.world.module.camera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.my.world.core.System;
import com.my.world.core.*;
import com.my.world.core.util.Pool;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.common.Script;
import com.my.world.module.render.RenderSystem;

import java.util.*;

public class CameraSystem extends BaseSystem implements EntityListener, System.OnUpdate, System.OnStart {

    protected RenderSystem renderSystem;

    protected final EntityFilter beforeRenderFilter = entity -> entity.contain(BeforeRender.class);
    protected final EntityFilter afterRenderFilter = entity -> entity.contain(AfterRender.class);
    protected final Map<Entity, ArrayList<Camera>> cameras = new LinkedHashMap<>();
    protected final List<Camera> sortedCameras = new LinkedList<>();
    protected final Pool<ArrayList<Camera>> pool = new Pool<>(ArrayList::new);

    @Override
    public boolean canHandle(Entity entity) {
        return entity.contain(Camera.class);
    }

    @Override
    public void afterAdded(Scene scene) {
        super.afterAdded(scene);
        scene.getEntityManager().addFilter(beforeRenderFilter);
        scene.getEntityManager().addFilter(afterRenderFilter);
    }

    @Override
    public void start(Scene scene) {
        renderSystem = scene.getSystemManager().getSystem(RenderSystem.class);
    }

    @Override
    public void dispose() {
        renderSystem = null;
        cameras.clear();
        sortedCameras.clear();
    }

    @Override
    public void afterEntityAdded(Entity entity) {
        List<Camera> list = entity.getComponents(Camera.class);
        if (list.isEmpty()) throw new RuntimeException("No camera component found in this entity: id=" + entity.getId());
        list.forEach(c -> c.registerToCameraSystem(scene, entity, this));

        ArrayList<Camera> cameraList = pool.obtain();
        cameraList.addAll(list);
        cameras.put(entity, cameraList);
        sortedCameras.addAll(cameraList);
        Collections.sort(sortedCameras);
    }

    @Override
    public void afterEntityRemoved(Entity entity) {
        ArrayList<Camera> cameraList = cameras.remove(entity);
        if (cameraList == null) throw new RuntimeException("Unregistered entity: id=" + entity.getId());
        sortedCameras.removeAll(cameraList);

        cameraList.forEach(c -> c.unregisterFromCameraSystem(scene, entity, this));

        cameraList.clear();
        pool.free(cameraList);
    }

    @Override
    public void update(float deltaTime) {
        renderSystem.begin();

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        for (Camera camera : sortedCameras) {
            if (camera.isActive()) {
                com.badlogic.gdx.graphics.Camera cam = camera.getCamera();
                if (cam == null) continue;
                resetViewport(
                        (int) (width * camera.getStartX()),
                        (int) (height * camera.getStartY()),
                        (int) (width * camera.getEndX() - width * camera.getStartX()),
                        (int) (height * camera.getEndY() - height * camera.getStartY())
                );
                render(cam);
            }
        }
        resetViewport(0, 0, width, height);

        renderSystem.end();
    }

    // ----- Protected ----- //

    protected void render(com.badlogic.gdx.graphics.Camera camera) {
        callAllBeforeRenderScript(camera);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        renderSystem.render(camera);

        callAllAfterRenderScript(camera);
    }

    public void callAllAfterRenderScript(com.badlogic.gdx.graphics.Camera camera) {
        for (Entity entity : scene.getEntityManager().getEntitiesByFilter(afterRenderFilter)) {
            for (AfterRender script : entity.getComponents(AfterRender.class)) {
                if (Component.isActive(script)) {
                    script.afterRender(camera);
                }
            }
        }
    }

    public void callAllBeforeRenderScript(com.badlogic.gdx.graphics.Camera camera) {
        for (Entity entity : scene.getEntityManager().getEntitiesByFilter(beforeRenderFilter)) {
            for (BeforeRender script : entity.getComponents(BeforeRender.class)) {
                if (Component.isActive(script)) {
                    script.beforeRender(camera);
                }
            }
        }
    }

    protected static void resetViewport(int x, int y, int width, int height) {
        Gdx.gl.glViewport(x, y, width, height);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
    }

    public interface BeforeRender extends Script {
        void beforeRender(com.badlogic.gdx.graphics.Camera cam);
    }

    public interface AfterRender extends Script {
        void afterRender(com.badlogic.gdx.graphics.Camera cam);
    }

}
