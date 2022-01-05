package com.my.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.my.game.builder.PrefabBuilder;
import com.my.game.builder.SceneBuilder;
import com.my.world.core.Engine;
import com.my.world.core.Scene;
import com.my.world.core.SceneManager;
import com.my.world.gdx.GdxEngine;

public class MyWorld extends ApplicationAdapter {

    private Engine engine;
    private SceneManager sceneManager;

    @Override
    public void create() {
        System.out.println("created");

        // ----- Init Bullet ----- //
        Bullet.init();

        // ----- Create Engine ----- //
        engine = new GdxEngine();
        sceneManager = engine.getSceneManager();

        // ----- Create & Save Assets ----- //
        SceneBuilder.initAllAssets(engine.getAssetsManager());
        PrefabBuilder.initAssets(engine);
//        engine.getAssetsManager().dumpAssetsToFile("assets.yml");
//
//        // ----- Load Assets ----- //
//        engine.getAssetsManager().loadAssetsFromFile("assets.yml");

        // ----- Create & Save Scene ----- //
        Scene scene = SceneBuilder.createScene(engine);
//        engine.getSceneManager().dumpSceneToFile(scene, "scene.yml");
//
//        // ----- Load Scene ----- //
//        engine.getSceneManager().removeScene(scene.getId());
//        engine.getSceneManager().loadSceneFromFile("scene.yml");

    }

    @Override
    public void render() {
        sceneManager.update(1 / 60f);
    }

    @Override
    public void dispose() {
        engine.dispose();
    }
}
