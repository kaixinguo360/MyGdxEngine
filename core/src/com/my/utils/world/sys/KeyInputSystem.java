package com.my.utils.world.sys;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.my.utils.world.BaseSystem;
import com.my.utils.world.Entity;
import com.my.utils.world.Scene;
import com.my.utils.world.System;
import com.my.utils.world.com.Script;
import lombok.Getter;

public class KeyInputSystem extends BaseSystem implements System.OnStart {

    @Getter
    private final InputMultiplexer inputMultiplexer;

    public KeyInputSystem() {
        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                for (Entity entity : KeyInputSystem.this.getEntities()) {
                    for (OnKeyDown script : entity.getComponents(OnKeyDown.class)) {
                        script.keyDown(scene, entity, keycode);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean isHandleable(Entity entity) {
        return entity.contain(OnKeyDown.class);
    }

    @Override
    public void start(Scene scene) {
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    public interface OnKeyDown extends Script {
        void keyDown(Scene scene, Entity entity, int keycode);
    }
}
