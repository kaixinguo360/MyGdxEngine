package com.my.demo.script;

import com.badlogic.gdx.Input;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.TimeManager;
import com.my.world.module.input.InputSystem;
import com.my.world.module.script.ScriptSystem;

public class PauseScript implements ScriptSystem.OnStart, InputSystem.OnKeyDown {

    @Config
    private int keycode = Input.Keys.P;

    private Scene scene;
    private Float lastTimeScale;

    @Override
    public void start(Scene scene, Entity entity) {
        this.scene = scene;
    }

    @Override
    public void keyDown(int keycode) {
        if (keycode == this.keycode) {
            TimeManager timeManager = scene.getTimeManager();
            float timeScale = timeManager.getTimeScale();
            if (timeScale == 0) {
                timeManager.setTimeScale((lastTimeScale != null && lastTimeScale != 0) ? lastTimeScale : 1);
            } else {
                lastTimeScale = timeScale;
                timeManager.setTimeScale(0);
            }
        }
    }
}
