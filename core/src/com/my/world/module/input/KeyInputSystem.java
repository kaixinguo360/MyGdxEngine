package com.my.world.module.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.core.System;
import com.my.world.module.common.BaseSystem;
import com.my.world.module.common.Script;
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
