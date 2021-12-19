package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Script;

import java.util.HashMap;
import java.util.Map;

public class Scripts {

    private static final Vector3 TMP_1 = new Vector3();

    public static class RemoveScript extends Script implements Script.OnStart, Script.OnUpdate {

        private Position position;
        private boolean handleable;

        @Override
        public void start(World world, Entity entity) {
            this.handleable = entity.contain(Position.class);
            this.position = entity.getComponent(Position.class);
        }

        @Override
        public void update(World world, Entity entity) {
            if (!handleable) return;
            float dst = position.transform.getTranslation(TMP_1).dst(0, 0, 0);
            if (dst > 10000) {
                world.getEntityManager().getBatch().removeEntity(entity.getId());
            }
        }
    }

    public static class ExitScript extends Script implements Script.OnKeyDown {
        @Override
        public void keyDown(World world, Entity entity, int keycode) {
            if (keycode == Input.Keys.ESCAPE) Gdx.app.exit();
        }
    }

    public static class GUIScript extends Script implements Script.OnStart, Script.OnUpdate {

        public Stage stage;
        public Skin skin;
        private VerticalGroup group;
        private Map<String, Actor> widgets = new HashMap<>();

        private Aircrafts.Aircraft aircraft;

        @Override
        public void start(World world, Entity entity) {

            Entity aircraftEntity = world.getEntityManager().getEntity("Aircraft-6");
            aircraft = aircraftEntity.getComponent(Aircrafts.Aircraft.class);

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
                    "\nV = " + Math.floor(aircraft.getVelocity()) +
                            "\nH = " + Math.floor(aircraft.getHeight()) + "\n");
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
}
