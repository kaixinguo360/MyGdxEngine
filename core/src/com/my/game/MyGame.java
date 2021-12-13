package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.my.utils.base.Base3DGame;
import com.my.utils.net.Client;
import com.my.utils.net.Server;
import com.my.utils.world.AssetsManager;
import com.my.utils.world.Entity;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.World;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.ScriptComponent;
import com.my.utils.world.loader.WorldLoader;
import com.my.utils.world.sys.*;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

public class MyGame extends Base3DGame {

    private static void initLoaders(LoaderManager loaderManager) {
        loaderManager.getLoaders().add(new Motions.Loader());
        loaderManager.getLoaders().add(new Collisions.Loader());
        loaderManager.getLoaders().add(new Constraints.Loader());
        loaderManager.getLoaders().add(new Aircrafts.AircraftLoader(loaderManager));
        loaderManager.getLoaders().add(new Aircrafts.AircraftControllerLoader());
        loaderManager.getLoaders().add(new Guns.GunLoader(loaderManager));
        loaderManager.getLoaders().add(new Guns.GunControllerLoader());
    }
    private static void initAssets(World world) {

        AssetsManager assetsManager = world.getAssetsManager();
        Collisions.initAssets(assetsManager);
        Constraints.initAssets(assetsManager);
        Motions.initAssets(assetsManager);
        Scripts.initAssets(assetsManager);
        Aircrafts.initAssets(assetsManager);
        Guns.initAssets(assetsManager);
        SceneBuilder.initAssets(assetsManager);

        // ----- Init Models ----- //
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();
        ArrayMap<String, Model> models = new ArrayMap<>();
        models.put("sky", skyModel);
        models.put("ground", mdBuilder.createBox(10000f, 0.01f, 20000f, new Material(ColorAttribute.createDiffuse(Color.WHITE)), attributes));

        // ----- Init Configs ----- //
        assetsManager.addAsset("sky", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("sky"), false));
        assetsManager.addAsset("ground", RenderSystem.RenderConfig.class, new RenderSystem.RenderConfig(models.get("ground")));

        assetsManager.addAsset("ground", PhysicsSystem.RigidBodyConfig.class, new PhysicsSystem.RigidBodyConfig(new btBoxShape(new Vector3(5000,0.005f,10000)), 0f));
    }

    private GameWorld gameWorld;

    private PerspectiveCamera camera;
    private Environment environment;
    private static Model skyModel;

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
                    Map config = gameWorld.loaderManager.getConfig(gameWorld.world, Map.class);
//                    Yaml yaml = new Yaml();
//                    String yamlConfig = yaml.dumpAsMap(config);
//                    Map loadedConfig = yaml.loadAs(yamlConfig, Map.class);

                    // ----- Load GameWorld ----- //
                    gameWorld = loadGameWorld(config);
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
        skyModel = assetManager.get("obj/sky.g3db", Model.class);
        skyModel.nodes.get(0).scale.scl(20);

        // ----- Init Bullet ----- //
        Bullet.init();

        // ----- Create GameWorld ----- //
        gameWorld = createGameWorld();
        addDisposable(gameWorld);

        // ----- Update Vehicles ----- //
        Aircrafts.Aircraft aircraft = gameWorld.world.getEntityManager().getEntity("Aircraft-6").getComponent(Aircrafts.Aircraft.class);
        Guns.Gun gun = gameWorld.world.getEntityManager().getEntity("Gun-0").getComponent(Guns.Gun.class);
        vehicles.add(aircraft);
        vehicles.add(gun);
    }

    private static GameWorld createGameWorld() {
        World world = new World();

        // Init World
        world.getSystemManager().addSystem(new RenderSystem());
        world.getSystemManager().addSystem(new PhysicsSystem());
        world.getSystemManager().addSystem(new SerializationSystem());
        world.getSystemManager().addSystem(new ConstraintSystem());
        world.getSystemManager().addSystem(new MotionSystem());
        world.getSystemManager().addSystem(new ScriptSystem());
        MyGame.initAssets(world);

        // ----- Init Static Objects ----- //
        MyInstance sky = new MyInstance(world.getAssetsManager(), "sky");
        sky.setId("sky");
        world.getEntityManager().addEntity(sky);
        MyInstance ground = new MyInstance(world.getAssetsManager(), "ground");
        ground.setId("ground");
        world.getEntityManager().addEntity(ground);

        // ----- Init Dynamic Objects ----- //
        Aircrafts.AircraftBuilder aircraftBuilder = new Aircrafts.AircraftBuilder(world);
        Guns.GunBuilder gunBuilder = new Guns.GunBuilder(world);
        SceneBuilder sceneBuilder = new SceneBuilder(world);
        for (int i = 0; i < 100; i++) {
            sceneBuilder.createBox(new Matrix4().translate(10, 0.5f, -10 * i), ground.getId());
            sceneBuilder.createBox(new Matrix4().translate(-10, 0.5f, -10 * i), ground.getId());
        }
        for (int i = 1; i < 5; i++) {
            sceneBuilder.createTower(new Matrix4().setToTranslation(-5, 0, -200 * i), 5 * i);
        }
        for (int x = -20; x <= 20; x+=40) {
            for (int y = 0; y <= 0; y+=20) {
                for (int z = -20; z <= 20; z+=20) {
                    aircraftBuilder.createAircraft(new Matrix4().translate(x, y, z), 4000, 40);
                }
            }
        }

        Entity aircraftEntity = aircraftBuilder.createAircraft(new Matrix4().translate(0, 0, 200), 8000, 40);
        ScriptComponent aircraftScriptComponent = new ScriptComponent();
        aircraftScriptComponent.script = world.getAssetsManager().getAsset("AircraftScript", ScriptSystem.Script.class);
        aircraftEntity.addComponent(aircraftScriptComponent);

        Entity gunEntity = gunBuilder.createGun("ground", new Matrix4().translate(0, 0, -20));
        ScriptComponent gunScriptComponent = new ScriptComponent();
        gunScriptComponent.script = world.getAssetsManager().getAsset("GunScript", ScriptSystem.Script.class);
        gunScriptComponent.disabled = true;
        gunEntity.addComponent(gunScriptComponent);

        // Init World Entity Filters
        world.update();

        // Create LoaderManager
        LoaderManager loaderManager = new LoaderManager();
        MyGame.initLoaders(loaderManager);
        loaderManager.getEnvironment().put("world", world);

        return new GameWorld(world, loaderManager);
    }

    private static GameWorld loadGameWorld(Map config) {
        LoaderManager loaderManager = new LoaderManager();
        MyGame.initLoaders(loaderManager);
        loaderManager.getLoader(WorldLoader.class).setBeforeLoadAssets(MyGame::initAssets);

        World world = loaderManager.load(config, World.class);
        world.update();

        return new GameWorld(world, loaderManager);
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
        gameWorld.motionSystem.update();
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
        secondaryView.getEntity().getComponent(ScriptComponent.class).disabled = true;
        mainView.getEntity().getComponent(ScriptComponent.class).disabled = false;
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

    private static class GameWorld implements Disposable {

        private final World world;
        private final RenderSystem renderSystem;
        private final PhysicsSystem physicsSystem;
        private final SerializationSystem serializationSystem;
        private final ConstraintSystem constraintSystem;
        private final MotionSystem motionSystem;
        private final ScriptSystem scriptSystem;
        private final LoaderManager loaderManager;

        public GameWorld(World world, LoaderManager loaderManager) {
            renderSystem = world.getSystemManager().getSystem(RenderSystem.class);
            physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
            serializationSystem = world.getSystemManager().getSystem(SerializationSystem.class);
            constraintSystem = world.getSystemManager().getSystem(ConstraintSystem.class);
            motionSystem = world.getSystemManager().getSystem(MotionSystem.class);
            scriptSystem = world.getSystemManager().getSystem(ScriptSystem.class);
            this.world = world;
            this.loaderManager = loaderManager;
        }

        @Override
        public void dispose() {
            this.world.dispose();
        }
    }
}
