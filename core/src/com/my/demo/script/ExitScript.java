package com.my.demo.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.my.world.module.input.InputSystem;

public class ExitScript implements InputSystem.OnKeyDown {
    @Override
    public void keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) Gdx.app.exit();
    }
}
