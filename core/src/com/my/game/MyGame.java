package com.my.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.game.builder.PrefabBuilder;
import com.my.game.builder.SceneBuilder;
import com.my.utils.base.BaseGame;
import com.my.utils.world.Engine;
import com.my.utils.world.Scene;
import com.my.utils.world.sys.KeyInputSystem;

public class MyGame extends BaseGame {

    private Engine engine;
    private Scene scene;
    private InputAdapter inputAdapter;

    @Override
    public void create() {
        super.create();

        // ----- Loading Assets ----- //
        assetManager.load("obj/sky.g3db", Model.class);
        waitLoad();
    }

    @Override
    protected void doneLoading() {
        System.out.println("doneLoading");

        // ----- Create Models ----- //
        SceneBuilder.skyModel = assetManager.get("obj/sky.g3db", Model.class);
        SceneBuilder.skyModel.nodes.get(0).scale.scl(20);

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
                    addDisposable(scene);
                    scene.getSystemManager().getSystem(KeyInputSystem.class).getInputMultiplexer().addProcessor(inputAdapter);
                }
                return false;
            }
        };

        // ----- Create Engine ----- //
        engine = new Engine();

        // ----- Init AssetsManager ----- //
        SceneBuilder.initAllAssets(engine.getAssetsManager());

        // ----- Init PrefabBuilder ----- //
        PrefabBuilder.initAssets(engine);

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
    protected void myRender() {
        scene.update(1 / 60f);
    }
}
