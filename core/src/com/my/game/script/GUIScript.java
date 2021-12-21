package com.my.game.script;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.sys.ScriptSystem;

import java.util.HashMap;
import java.util.Map;

public class GUIScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate {

    public Stage stage;
    public Skin skin;
    private VerticalGroup group;
    private final Map<String, Actor> widgets = new HashMap<>();

    private AircraftScript aircraftScript;

    @Override
    public void start(World world, Entity entity) {

        Entity aircraftEntity = world.getEntityManager().findEntityByName("Aircraft-6");
        aircraftScript = aircraftEntity.getComponent(AircraftScript.class);

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

        // Add Label
        Label label = new Label("", skin);
        label.getStyle().fontColor = Color.DARK_GRAY;
        addWidget("label", label);

    }

    @Override
    public void update(World world, Entity entity) {
        getWidget("label", Label.class).setText(
                "\nV = " + Math.floor(aircraftScript.getVelocity()) +
                        "\nH = " + Math.floor(aircraftScript.getHeight()) + "\n");
        stage.act();
        stage.draw();
    }

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
