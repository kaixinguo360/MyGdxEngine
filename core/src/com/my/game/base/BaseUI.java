package com.my.game.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseUI {

    public Stage stage;

    protected Skin skin;

    private VerticalGroup group;
    private Map<String, Actor> widgets = new HashMap<>();

    void init() {

        // Create Skin
        skin = new Skin(Gdx.files.internal("skin/neon-ui.json"));

        // Create Stage
        stage = new Stage(new ScreenViewport());

        // Create Outer Table
        Table outerTable = new Table();
        outerTable.setFillParent(true);
        outerTable.right().top();
        stage.addActor(outerTable);

        // Create Window
        Window window = new Window("", skin);
        outerTable.add(window).width(150);

        // Create Group
        group = new VerticalGroup();
        group.fill();
        window.add(group);

        addWidgets();
    }

    void render() {
        stage.act();
        stage.draw();
    }

    void dispose() {
        stage.dispose();
    }

    void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    // -------------------- Public -------------------- //

    protected abstract void addWidgets();

    protected void addWidget(String key, Actor widget) {
        group.addActor(widget);
        widgets.put(key, widget);
    }

    public <T extends Actor> T getWidget(String key, Class<T> type) {
        return (T) widgets.get(key);
    }
}
