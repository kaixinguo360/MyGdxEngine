package com.my.demo.script;

import com.badlogic.gdx.Input;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.SceneManager;
import com.my.world.module.input.KeyInputSystem;

public class ReloadScript implements KeyInputSystem.OnKeyDown {

    @Override
    public void keyDown(Scene scene, Entity entity, int keycode) {
        if (keycode == Input.Keys.ENTER) {
            Engine engine = scene.getEngine();
            SceneManager sceneManager = engine.getSceneManager();

            // ----- Get Config ----- //
            String yamlConfig = sceneManager.dumpSceneToYaml(scene);
            System.out.println(yamlConfig);

            // ----- Load Scene ----- //
            sceneManager.removeScene(scene.getId());
            sceneManager.loadSceneFromYaml(yamlConfig);
        }
    }
}
