package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.my.utils.base.BaseGame;
import com.my.utils.world.Entity;
import com.my.utils.world.World;

public class MyGame extends BaseGame {

    private World world;

    private Aircrafts.Aircraft aircraft;

    @Override
    public void create() {
        super.create();

        // ----- Loading Assets ----- //
        assetManager.load("obj/sky.g3db", Model.class);
        waitLoad(true);
    }

    @Override
    protected void initUI() {

        inputMultiplexer.addProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                world.keyDown(keycode);
                if (keycode == Input.Keys.ENTER) {

                    // ----- Get Config ----- //
                    String yamlConfig = LoadUtil.saveWorldToYaml(world);
                    System.out.println(yamlConfig);

                    // ----- Load World ----- //
                    world = LoadUtil.loadWorldFromYaml(yamlConfig);
                    addDisposable(world);

                    // ----- Get Aircraft ----- //
                    Entity aircraftEntity = world.getEntityManager().getEntity("Aircraft-6");
                    aircraft = aircraftEntity.getComponent(Aircrafts.Aircraft.class);
                }
                return false;
            }
        });

        // Add Label
        Label label = new Label("", ui.skin);
        label.getStyle().fontColor = Color.DARK_GRAY;
        ui.addWidget("label", label);
    }

    @Override
    protected void doneLoading() {
        System.out.println("doneLoading");

        // ----- Create Models ----- //
        WorldBuilder.skyModel = assetManager.get("obj/sky.g3db", Model.class);
        WorldBuilder.skyModel.nodes.get(0).scale.scl(20);

        // ----- Init Bullet ----- //
        Bullet.init();

        // ----- Create & Save World ----- //
        world = WorldBuilder.createWorld();
//        addDisposable(world);
//        LoadUtil.saveWorldToFile(world, "world.yml");
//
//        // ----- Load World ----- //
//        world = LoadUtil.loadWorldFromFile("world.yml");
//        addDisposable(world);

        // ----- Get Aircraft ----- //
        Entity aircraftEntity = world.getEntityManager().getEntity("Aircraft-6");
        aircraft = aircraftEntity.getComponent(Aircrafts.Aircraft.class);
    }

    @Override
    protected void myRender() {

        // Update World
        world.update(1 / 60f);

        // Update UI
        ui.getWidget("label", Label.class).setText(
                "\nV = " + Math.floor(aircraft.getVelocity()) +
                        "\nH = " + Math.floor(aircraft.getHeight()) + "\n");

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }
}
