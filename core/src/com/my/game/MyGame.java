package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.base.Base3DGame;
import com.my.utils.net.Client;
import com.my.utils.net.Server;
import com.my.utils.world.Entity;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.World;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Script;
import com.my.utils.world.loader.WorldLoader;
import com.my.utils.world.sys.*;

import java.net.SocketException;
import java.net.UnknownHostException;

public class MyGame extends Base3DGame {

    private GameWorld gameWorld;

    private PerspectiveCamera camera;
    private Environment environment;

    private Array<CameraController> vehicles = new Array<>();
    private Server server;
    private String receivedData = null;
    private long receivedTime;
    private int delay = 500;
    private float delayD;

    @Override
    public void create() {
        // ----- Net ----- //
        try {
            server = new Server("127.0.0.1", 1001, "127.0.0.1", 1002);
        } catch (SocketException | UnknownHostException e) {
            try {
                Client client = new Client("127.0.0.1", 1002, (data, time) -> {
                    MyGame.this.receivedData = data;
                    MyGame.this.receivedTime = time;
                });
            } catch (SocketException e1) {
                throw new RuntimeException(e1);
            }
        }

        super.create();

        // ----- Create Environment ----- //
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.2f, -0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.2f, 0.8f, -1f));

        // ----- Init Camera ----- //
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.far = 2000;
        camera.near = 0.1f;
        camera.position.set(0, 0, 0);
        camera.update();

        // ----- Loading Assets ----- //
        assetManager.load("obj/sky.g3db", Model.class);
        waitLoad(true);
    }

    @Override
    protected void initUI() {

        inputMultiplexer.addProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) Gdx.app.exit();
                if (keycode == Input.Keys.TAB) changeView();
                if (keycode == Input.Keys.SHIFT_LEFT) mainView.changeCamera();
                if (keycode == Input.Keys.ENTER) {

                    // ----- Get Config ----- //
                    String yamlConfig = LoadUtil.saveWorldToYaml(gameWorld);
                    System.out.println(yamlConfig);

                    // ----- Load GameWorld ----- //
                    gameWorld = LoadUtil.loadWorldFromYaml(yamlConfig);
                    addDisposable(gameWorld);

                    // ----- Update Vehicles ----- //
                    Aircrafts.Aircraft aircraft = gameWorld.world.getEntityManager().getEntity("Aircraft-6").getComponent(Aircrafts.Aircraft.class);
                    Guns.Gun gun = gameWorld.world.getEntityManager().getEntity("Gun-0").getComponent(Guns.Gun.class);
                    vehicles.set(0, aircraft);
                    vehicles.set(1, gun);
                }
                return false;
            }
        });

        // Add Title Label
        Label title = new Label("", ui.skin);
        ui.addWidget("title", title);

        // ----- Net----- //
        if (server == null) {
            title.setText("Client");
        } else {
            title.setText("Server");

            // Add Slider
            Slider slider = new Slider(0, 1000, 10, false, ui.skin);
            slider.setValue(delay);
            slider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    delay = (int) slider.getValue();
                    ui.getWidget("label", Label.class).setText("Delay: " + delay);
                }
            });
            ui.addWidget("slider", slider);

            // Add Button
            ui.addWidget("btn", new TextButton("Explosion!", ui.skin));
            ui.getWidget("btn", Button.class).addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
//                    mainView.getVehicle().explode();
                    System.out.println("Explosion!");
                }
            });

            // Add Label
            Label label = new Label("", ui.skin);
            label.setWrap(true);
//        label.getStyle().fontColor.set(1, 1, 1, 1);
            ui.addWidget("label", label);
        }
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

        // ----- Update Vehicles ----- //
        Aircrafts.Aircraft aircraft = gameWorld.world.getEntityManager().getEntity("Aircraft-6").getComponent(Aircrafts.Aircraft.class);
        Guns.Gun gun = gameWorld.world.getEntityManager().getEntity("Gun-0").getComponent(Guns.Gun.class);
        vehicles.add(aircraft);
        vehicles.add(gun);
    }

    @Override
    protected void myRender() {
        // ----- Net----- //
        float deltaTime = 1 / 60f;
        if (server != null) {
            delayD += Gdx.graphics.getDeltaTime();
            if (delayD >= delay*0.001) {
                server.send(gameWorld.serializationSystem.serialize("serializable"));
                delayD = 0;
            }
        } else {
            if (receivedData != null) {
                gameWorld.serializationSystem.deserialize(receivedData);
                deltaTime = (System.currentTimeMillis() - receivedTime) * 0.001f;
                System.out.println("[" + deltaTime + "] Deserialize Received Data: " + receivedData.hashCode());
                receivedData = null;
                receivedTime = 0;
            }
        }

        // Render
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mainView.setCamera(camera);
        gameWorld.world.getEntityManager().getEntity("sky").getComponent(Position.class).transform.setToTranslation(camera.position);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        gameWorld.renderSystem.render(camera, environment);
        Gdx.gl.glViewport(0, Gdx.graphics.getHeight() - 250, 400, 250);
        secondaryView.setCamera(camera);
        gameWorld.world.getEntityManager().getEntity("sky").getComponent(Position.class).transform.setToTranslation(camera.position);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        gameWorld.renderSystem.render(camera, environment);

        // Update UI
        Aircrafts.Aircraft aircraft = (Aircrafts.Aircraft) vehicles.get(0);
        ui.getWidget("label", Label.class).setText(
                "  Velocity: " + Math.floor(aircraft.getVelocity()) +
                        "\n  Height: " + Math.floor(aircraft.getHeight()));

        // Update World
        gameWorld.world.update();
        gameWorld.constraintSystem.update();
        gameWorld.physicsSystem.update(deltaTime);
        gameWorld.scriptSystem.update();
        gameWorld.world.getEntityManager().getBatch().commit();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    // ----- Custom ----- //
    private View mainView = new View(0, 0, "Aircraft-6");
    private View secondaryView = new View(1, 0, "Gun-0");
    private void changeView() {
        View tmp = mainView;
        mainView = secondaryView;
        secondaryView = tmp;
        secondaryView.getEntity().getComponents(Script.class).get(0).disabled = true;
        mainView.getEntity().getComponents(Script.class).get(0).disabled = false;
    }

    private class View {
        int cameraIndex;
        int vehicleIndex;
        String entityId;
        private View(int vehicleIndex, int cameraIndex, String entityId) {
            this.vehicleIndex = vehicleIndex;
            this.cameraIndex = cameraIndex;
            this.entityId = entityId;
        }
        private void setCamera(PerspectiveCamera camera) {
            vehicles.get(vehicleIndex).setCamera(camera, cameraIndex);
        }
        private CameraController getVehicle() {
            return vehicles.get(vehicleIndex);
        }
        private Entity getEntity() {
            return gameWorld.world.getEntityManager().getEntity(entityId);
        }
        private void changeCamera() {
            cameraIndex = (cameraIndex + 1) % 2;
        }
        private void changeVehicle() {
            vehicleIndex = (vehicleIndex + 1) % vehicles.size;
        }
    }

    public static class GameWorld implements Disposable {

        public final World world;
        public final RenderSystem renderSystem;
        public final PhysicsSystem physicsSystem;
        public final SerializationSystem serializationSystem;
        public final ConstraintSystem constraintSystem;
        public final ScriptSystem scriptSystem;
        public final LoaderManager loaderManager;

        public GameWorld(World world, LoaderManager loaderManager) {
            renderSystem = world.getSystemManager().getSystem(RenderSystem.class);
            physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
            serializationSystem = world.getSystemManager().getSystem(SerializationSystem.class);
            constraintSystem = world.getSystemManager().getSystem(ConstraintSystem.class);
            scriptSystem = world.getSystemManager().getSystem(ScriptSystem.class);
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
