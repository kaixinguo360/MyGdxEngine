package com.my.game.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.my.utils.world.Entity;
import com.my.utils.world.Scene;
import com.my.utils.world.sys.KeyInputSystem;

public class ExitScript implements KeyInputSystem.OnKeyDown {
    @Override
    public void keyDown(Scene scene, Entity entity, int keycode) {
        if (keycode == Input.Keys.ESCAPE) Gdx.app.exit();
    }
}
