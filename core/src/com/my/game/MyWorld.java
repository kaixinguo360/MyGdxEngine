package com.my.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.game.builder.PrefabBuilder;
import com.my.game.builder.SceneBuilder;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
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

                    // ----- Get Config ----- //
                    String yamlConfig = engine.dumpSceneToYaml(scene);
                    System.out.println(yamlConfig);

                    // ----- Load Scene ----- //
                    scene = engine.loadSceneFromYaml(yamlConfig);
                    scene.getSystemManager().getSystem(KeyInputSystem.class).getInputMultiplexer().addProcessor(inputAdapter);
                }
                return false;
            }
        };

        // ----- Create Engine ----- //
        engine = new Engine();

        // ----- Create & Save Assets ----- //
        SceneBuilder.initAllAssets(engine.getAssetsManager());
        PrefabBuilder.initAssets(engine);
//        engine.dumpAssetsToFile("assets.yml");
//
//        // ----- Load Assets ----- //
//        engine.loadAssetsFromFile("assets.yml");

        // ----- Create & Save Scene ----- //
        scene = SceneBuilder.createScene(engine);
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
        scene.update(1 / 60f);
    }

    @Override
    public void dispose() {
        scene.dispose();
    }
}
