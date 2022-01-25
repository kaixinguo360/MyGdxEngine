package com.my.demo;

import com.my.demo.builder.SceneBuilder;
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
        SceneBuilder.init(engine);
    }
    private void saveAssets() {
        engine.getAssetsManager().dumpAssetsToFile("assets.yml");
    }
    private void loadAssets() {
        engine.getAssetsManager().loadAssetsFromFile("assets.yml");
    }

    private Scene createScene() {
        Scene scene = newGLTFScene(true);
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
