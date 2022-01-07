package com.my.game;

import com.my.game.builder.PrefabBuilder;
import com.my.game.builder.SceneBuilder;
import com.my.world.core.Scene;
import com.my.world.gdx.GdxApplication;

public class MyWorld extends GdxApplication {

    @Override
    public void create() {
        super.create();

        // ----- Create & Save & Load Scene Assets ----- //
        createAssets();
//        saveAssets();
//        loadAssets();

        // ----- Create & Save & Load Scene ----- //
        Scene scene = createScene();
//        saveScene(scene);
//        loadScene();
    }

    private void createAssets() {
        SceneBuilder.initAllAssets(engine.getAssetsManager());
        PrefabBuilder.initAssets(engine);

    }
    private void saveAssets() {
        engine.getAssetsManager().dumpAssetsToFile("assets.yml");
    }
    private void loadAssets() {
        engine.getAssetsManager().loadAssetsFromFile("assets.yml");
    }

    private Scene createScene() {
        Scene scene = newScene();
        SceneBuilder.initScene(scene);
        return scene;
    }
    private void saveScene(Scene scene) {
        engine.getSceneManager().dumpSceneToFile(scene, "scene.yml");
    }
    private void loadScene() {
        engine.getSceneManager().loadSceneFromFile("scene.yml");
    }
}
