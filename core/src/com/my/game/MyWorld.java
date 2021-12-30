package com.my.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.game.builder.PrefabBuilder;
import com.my.game.builder.SceneBuilder;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.core.ScenesManager;
import com.my.world.gdx.GdxEngine;
import com.my.world.module.input.KeyInputSystem;

public class MyWorld extends ApplicationAdapter {

    private Engine engine;
    private Scene scene;
    private InputAdapter inputAdapter;

    @Override
    public void create() {
        System.out.println("created");

        // ----- Init Bullet ----- //
        Bullet.init();

        // ----- Init InputAdapter ----- //
        inputAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER) {
                    ScenesManager scenesManager = engine.getScenesManager();

                    // ----- Get Config ----- //
                    String yamlConfig = scenesManager.dumpSceneToYaml(scene);
                    System.out.println(yamlConfig);

                    // ----- Load Scene ----- //
                    scenesManager.removeScene("default");
                    scene = scenesManager.loadSceneFromYaml(yamlConfig);
                    engine.getScenesManager().setActivatedScene("default");
                    scene.getSystemManager().getSystem(KeyInputSystem.class).getInputMultiplexer().addProcessor(inputAdapter);
                }
                return false;
            }
        };

        // ----- Create Engine ----- //
        engine = new GdxEngine();

        // ----- Create & Save Assets ----- //
        SceneBuilder.initAllAssets(engine.getAssetsManager());
        PrefabBuilder.initAssets(engine);
//        engine.dumpAssetsToFile("assets.yml");
//
//        // ----- Load Assets ----- //
//        engine.loadAssetsFromFile("assets.yml");

        // ----- Create & Save Scene ----- //
        scene = SceneBuilder.createScene(engine);
        engine.getScenesManager().setActivatedScene(scene.getName());
//        addDisposable(scene);
//        engine.dumpSceneToFile(scene, "scene.yml");
//
//        // ----- Load Scene ----- //
//        scene = engine.loadSceneFromFile("scene.yml");
//        addDisposable(scene);

        scene.getSystemManager().getSystem(KeyInputSystem.class).getInputMultiplexer().addProcessor(inputAdapter);
    }

    @Override
    public void render() {
        engine.update(1 / 60f);
    }

    @Override
    public void dispose() {
        engine.dispose();
    }
}
