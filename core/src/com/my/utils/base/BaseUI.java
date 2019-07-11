package com.my.utils.base;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.HashMap;
import java.util.Map;

public class BaseUI {

    public Stage stage;
    public Skin skin;
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

    // -------------------- Public & Protected -------------------- //

    // Add Widget
    public void addWidget(String name, Actor widget) {
        group.addActor(widget);
        widgets.put(name, widget);
    }

    // Get Specific Widget By The Given Name
    public <T extends Actor> T getWidget(String name, Class<T> type) {
        return (T) widgets.get(name);
    }
}
