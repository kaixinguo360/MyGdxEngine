package com.my.world.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.world.core.*;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.input.InputSystem;
import com.my.world.module.physics.ConstraintSystem;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.render.DefaultRenderSystem;
import com.my.world.module.render.EnvironmentSystem;
import com.my.world.module.script.ScriptSystem;

import java.util.List;

public class GdxApplication extends ApplicationAdapter {

    protected Engine engine;
    protected SceneManager sceneManager;

    @Override
    public void create() {
        Bullet.init();
        engine = newEngine();
        sceneManager = engine.getSceneManager();
    }

    @Override
    public void render() {
        sceneManager.update(1 / 60f);
    }

    @Override
    public void dispose() {
        engine.dispose();
    }

    public Scene newScene() {
        Scene scene = engine.getSceneManager().newScene("default");
        SystemManager systemManager = scene.getSystemManager();
        systemManager.addSystem(new CameraSystem());
        systemManager.addSystem(new DefaultRenderSystem());
        systemManager.addSystem(new PhysicsSystem());
        systemManager.addSystem(new ScriptSystem());
        systemManager.addSystem(new EnvironmentSystem());
        systemManager.addSystem(new InputSystem());
        systemManager.addSystem(new ConstraintSystem());
        return scene;
    }

    public static Engine newEngine() {
        Engine engine = new Engine();
        List<Loader> loaders = engine.getLoaderManager().getLoaders();
        loaders.add(new SceneLoader());
        loaders.add(new Matrix4Loader());
        loaders.add(new Vector3Loader());
        loaders.add(new QuaternionLoader());
        loaders.add(new ColorLoader());
        loaders.add(new LoadableLoader());
        return engine;
    }
}
