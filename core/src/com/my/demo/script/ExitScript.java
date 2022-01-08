package com.my.demo.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.input.KeyInputSystem;

public class ExitScript implements KeyInputSystem.OnKeyDown {
    @Override
    public void keyDown(Scene scene, Entity entity, int keycode) {
        if (keycode == Input.Keys.ESCAPE) Gdx.app.exit();
    }
}
