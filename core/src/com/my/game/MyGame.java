package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.base.BaseGame;
import com.my.utils.world.Entity;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.World;
import com.my.utils.world.loader.WorldLoader;
import com.my.utils.world.sys.*;

public class MyGame extends BaseGame {

    private GameWorld gameWorld;

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
                gameWorld.scriptSystem.keyDown(keycode);
                if (keycode == Input.Keys.ENTER) {

                    // ----- Get Config ----- //
                    String yamlConfig = LoadUtil.saveWorldToYaml(gameWorld);
                    System.out.println(yamlConfig);

                    // ----- Load GameWorld ----- //
                    gameWorld = LoadUtil.loadWorldFromYaml(yamlConfig);
                    addDisposable(gameWorld);

                    // ----- Get Aircraft ----- //
                    Entity aircraftEntity = gameWorld.world.getEntityManager().getEntity("Aircraft-6");
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

        // ----- Create / Save GameWorld ----- //
        gameWorld = WorldBuilder.createWorld();
//        addDisposable(gameWorld);
//        LoadUtil.saveWorldToFile(gameWorld, "world.yml");
//
//        // ----- Load GameWorld ----- //
//        gameWorld = LoadUtil.loadWorldFromFile("world.yml");
//        addDisposable(gameWorld);

        // ----- Get Aircraft ----- //
        Entity aircraftEntity = gameWorld.world.getEntityManager().getEntity("Aircraft-6");
        aircraft = aircraftEntity.getComponent(Aircrafts.Aircraft.class);
    }

    @Override
    protected void myRender() {

        // Update World
        gameWorld.world.update();
        gameWorld.cameraSystem.render();
        gameWorld.constraintSystem.update();
        gameWorld.physicsSystem.update(Gdx.graphics.getDeltaTime());
        gameWorld.scriptSystem.update();
        gameWorld.world.getEntityManager().getBatch().commit();

        // Update UI
        ui.getWidget("label", Label.class).setText(
                "\nV = " + Math.floor(aircraft.getVelocity()) +
                        "\nH = " + Math.floor(aircraft.getHeight()) + "\n");

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public static class GameWorld implements Disposable {

        public final World world;
        public final RenderSystem renderSystem;
        public final PhysicsSystem physicsSystem;
        public final ConstraintSystem constraintSystem;
        public final ScriptSystem scriptSystem;
        public final CameraSystem cameraSystem;
        public final LoaderManager loaderManager;

        public GameWorld(World world, LoaderManager loaderManager) {
            renderSystem = world.getSystemManager().getSystem(RenderSystem.class);
            physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
            constraintSystem = world.getSystemManager().getSystem(ConstraintSystem.class);
            scriptSystem = world.getSystemManager().getSystem(ScriptSystem.class);
            cameraSystem = world.getSystemManager().getSystem(CameraSystem.class);
            this.world = world;
            this.loaderManager = loaderManager;
        }

        @Override
        public void dispose() {
            this.world.dispose();
        }
    }

    public static class GameLoaderManager extends LoaderManager {
        public GameLoaderManager() {
            getLoader(WorldLoader.class).setBeforeLoadAssets(WorldBuilder::initAssets);
        }
    }
}
