package com.my.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.game.builder.WorldBuilder;
import com.my.utils.base.BaseGame;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.World;
import com.my.utils.world.sys.KeyInputSystem;

public class MyGame extends BaseGame {

    private World world;
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
        WorldBuilder.skyModel = assetManager.get("obj/sky.g3db", Model.class);
        WorldBuilder.skyModel.nodes.get(0).scale.scl(20);

        // ----- Init Bullet ----- //
        Bullet.init();

        // ----- Init InputAdapter ----- //
        inputAdapter = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER) {

                    // ----- Get Config ----- //
                    String yamlConfig = LoadUtil.dumpWorldToYaml(world);
                    System.out.println(yamlConfig);

                    // ----- Load World ----- //
                    world = LoadUtil.loadWorldFromYaml(yamlConfig);
                    addDisposable(world);
                    world.getSystemManager().getSystem(KeyInputSystem.class).getInputMultiplexer().addProcessor(inputAdapter);
                }
                return false;
            }
        };

        // ----- Init AssetsManager ----- //
        WorldBuilder.initAllAssets(LoadUtil.assetsManager);

        // ----- Init LoaderManager ----- //
        LoadUtil.loaderManager.getCommonContext().setEnvironment(AssetsManager.CONTEXT_FIELD_NAME, LoadUtil.assetsManager);

        // ----- Create & Save World ----- //
        world = WorldBuilder.createWorld(LoadUtil.assetsManager);
//        addDisposable(world);
//        LoadUtil.saveWorldToFile(world, "world.yml");
//
//        // ----- Load World ----- //
//        world = LoadUtil.loadWorldFromFile("world.yml");
//        addDisposable(world);

        world.getSystemManager().getSystem(KeyInputSystem.class).getInputMultiplexer().addProcessor(inputAdapter);
    }

    @Override
    protected void myRender() {
        world.update(1 / 60f);
    }
}
