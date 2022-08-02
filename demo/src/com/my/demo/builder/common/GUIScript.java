package com.my.demo.builder.common;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.my.demo.builder.aircraft.AircraftScript;
import com.my.world.core.Config;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.script.ScriptSystem;

import java.util.HashMap;
import java.util.Map;

public class GUIScript implements ScriptSystem.OnStart, ScriptSystem.OnUpdate {

    @Config
    public Entity targetEntity;

    public Stage stage;
    public Skin skin;
    private VerticalGroup group;
    private ShapeRenderer shapeRenderer;
    private final Map<String, Actor> widgets = new HashMap<>();

    private EmitterScript emitterScript;

    @Override
    public void start(Scene scene, Entity entity) {

        emitterScript = targetEntity.getComponent(AircraftScript.class);
        this.shapeRenderer = new ShapeRenderer();

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
    public void update(Scene scene, Entity entity) {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        Gdx.gl.glEnable(GL20.GL_BLEND);

        // Reset ShapeRenderer
        shapeRenderer.setProjectionMatrix(new Matrix4().scl(2f / Gdx.graphics.getWidth(), -2f / Gdx.graphics.getHeight(), 0).translate(-Gdx.graphics.getWidth() / 2f, -Gdx.graphics.getHeight() / 2f, 0));
        shapeRenderer.setTransformMatrix(new Matrix4());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw Square
        shapeRenderer.setColor(1, 1, 1, 0.2f);
        shapeRenderer.rectLine(width / 2 - 50, height / 2, width / 2 + 50, height / 2, 100);

        // Draw Cross
        shapeRenderer.setColor(0, 0, 0, 0.2f);
        shapeRenderer.rectLine(width / 2 - 100, height / 2, width / 2 + 100, height / 2, 2);
        shapeRenderer.rectLine(width / 2, height / 2 - 100, width / 2, height / 2 + 100, 2);
        shapeRenderer.end();

        // Update Stage
        getWidget("label", Label.class).setText(
                "\nV = " + Math.floor(emitterScript.getVelocity()) +
                        "\nH = " + Math.floor(emitterScript.getHeight()) + "\n");
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
