package com.my.demo;

import com.my.world.core.*;
import com.my.world.enhanced.EnhancedApplication;

public class DemoApplication extends EnhancedApplication {

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
        Scene scene = newScene();
        SceneBuilder.build(scene);
        return scene;
    }
    private void saveScene(Scene scene) {
        engine.getSceneManager().dumpSceneToFile(scene, "scene.yml");
    }
    private void loadScene() {
        engine.getSceneManager().loadSceneFromFile("scene.yml");
    }
}
