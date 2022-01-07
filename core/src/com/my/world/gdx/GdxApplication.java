package com.my.world.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.core.SceneManager;
import com.my.world.core.SystemManager;
import com.my.world.module.input.KeyInputSystem;
import com.my.world.module.physics.ConstraintSystem;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.render.CameraSystem;
import com.my.world.module.render.EnvironmentSystem;
import com.my.world.module.render.RenderSystem;
import com.my.world.module.script.ScriptSystem;

public class GdxApplication extends ApplicationAdapter {

    protected Engine engine;
    protected SceneManager sceneManager;

    @Override
    public void create() {
        Bullet.init();
        engine = new GdxEngine();
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
        systemManager.addSystem(new RenderSystem());
        systemManager.addSystem(new PhysicsSystem());
        systemManager.addSystem(new ScriptSystem());
        systemManager.addSystem(new EnvironmentSystem());
        systemManager.addSystem(new KeyInputSystem());
        systemManager.addSystem(new ConstraintSystem());
        return scene;
    }
}
