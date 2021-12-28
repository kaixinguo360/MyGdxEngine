package com.my.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.game.builder.SceneBuilder;
import com.my.utils.base.BaseGame;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Scene;
import com.my.utils.world.sys.KeyInputSystem;

public class MyGame extends BaseGame {

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
                    String yamlConfig = LoadUtil.dumpSceneToYaml(scene);
                    System.out.println(yamlConfig);

                    // ----- Load Scene ----- //
                    scene = LoadUtil.loadSceneFromYaml(yamlConfig);
                    addDisposable(scene);
                    scene.getSystemManager().getSystem(KeyInputSystem.class).getInputMultiplexer().addProcessor(inputAdapter);
                }
                return false;
            }
        };

        // ----- Init AssetsManager ----- //
        SceneBuilder.initAllAssets(LoadUtil.assetsManager);

        // ----- Init LoaderManager ----- //
        LoadUtil.loaderManager.getContext().setEnvironment(AssetsManager.CONTEXT_FIELD_NAME, LoadUtil.assetsManager);

        // ----- Create & Save Scene ----- //
        scene = SceneBuilder.createScene(LoadUtil.assetsManager);
//        addDisposable(scene);
//        LoadUtil.dumpSceneToFile(scene, "scene.yml");
//
//        // ----- Load Scene ----- //
//        scene = LoadUtil.loadSceneFromFile("scene.yml");
//        addDisposable(scene);

        scene.getSystemManager().getSystem(KeyInputSystem.class).getInputMultiplexer().addProcessor(inputAdapter);
    }

    @Override
    protected void myRender() {
        scene.update(1 / 60f);
    }
}
