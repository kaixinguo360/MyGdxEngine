package com.my.world.enhanced.script;

import com.badlogic.gdx.Input;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.SceneManager;
import com.my.world.module.input.InputSystem;
import com.my.world.module.script.ScriptSystem;

public class ReloadScript implements ScriptSystem.OnStart, InputSystem.OnKeyDown {

    private Scene scene;

    @Override
    public void start(Scene scene, Entity entity) {
        this.scene = scene;
    }

    @Override
    public void keyDown(int keycode) {
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
