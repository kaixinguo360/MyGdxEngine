package com.my.game.base;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.my.game.MyUI;

public abstract class BaseGame extends ApplicationAdapter {

    protected InputMultiplexer inputMultiplexer = new InputMultiplexer();
    protected BaseUI ui;

    @Override
    public void create() {
        // Set InputMultiplexer
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Init UI
        ui = new MyUI();
        ui.init();
        inputMultiplexer.addProcessor(ui.stage);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        myRender();
        ui.render();
    }

    protected abstract void myRender();

    @Override
    public void resize(int width, int height) {
        ui.resize(width, height);
    }

    @Override
    public void dispose() {
        ui.dispose();
    }
}
