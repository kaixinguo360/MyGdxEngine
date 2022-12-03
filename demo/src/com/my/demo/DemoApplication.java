package com.my.demo;

import com.my.world.core.Scene;
import com.my.world.enhanced.EnhancedApplication;

public class DemoApplication extends EnhancedApplication {

    private Scene scene;

    @Override
    public void create() {
        super.create();

        // Create
        createAssets();
        scene = createScene();

//        // Save
//        saveScene(scene);
//        saveAssets();

//        // Load
//        if (scene != null) engine.getSceneManager().removeScene(scene.getId());
//        engine.getAssetsManager().skipRepeat = true;
//        loadAssets();
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
