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
import com.badlogic.gdx.math.Quaternion;
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

    private static final Vector3 tmpV = new Vector3();
    private static final Matrix4 tmpM = new Matrix4();
    private static final Quaternion tmpQ = new Quaternion();

    private Environment environment;
    private World world;
    private RenderSystem renderSystem;
    private PhysicsSystem physicsSystem;
    private SerializationSystem serializationSystem;
    private ConstraintSystem constraintSystem;
    private MotionSystem motionSystem;
    private ScriptSystem scriptSystem;
    private LoaderManager loaderManager;
    private Array<CameraController> vehicles = new Array<>();
    private Server server;
    private String receivedData = null;
    private long receivedTime;
    private int delay = 500;
    private float delayD;
    private PerspectiveCamera camera;
    private Model skyModel;
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

        // ----- Init Camera ----- //
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.far = 2000;
        camera.near = 0.1f;
        camera.position.set(0, 0, 0);
        camera.update();

        // ----- Init World ----- //
        Bullet.init();
        world = new World();
        // Create renderSystem
        renderSystem = world.getSystemManager().addSystem(new RenderSystem());
        addDisposable(renderSystem);
        // Create physicsSystem
        physicsSystem = world.getSystemManager().addSystem(new PhysicsSystem());
        Collisions.init(world);
        addDisposable(physicsSystem);
        // Create serializationSystem
        serializationSystem = world.getSystemManager().addSystem(new SerializationSystem());
        addDisposable(serializationSystem);
        // Create constraintSystem
        constraintSystem = world.getSystemManager().addSystem(new ConstraintSystem());
        Constraints.init(world);
        addDisposable(constraintSystem);
        // Create MotionSystem
        motionSystem = world.getSystemManager().addSystem(new MotionSystem());
        Motions.init(world);
        addDisposable(motionSystem);
        // Create ScriptSystem
        scriptSystem = world.getSystemManager().addSystem(new ScriptSystem());
        // Create ObjectBuilder
        SceneBuilder.init(world);
        Aircrafts.initAssets(world.getAssetsManager());
        Guns.initAssets(world.getAssetsManager());
        Scripts.initAssets(world.getAssetsManager());
        // Create LoaderManager
        loaderManager = new LoaderManager();
        loaderManager.getLoaders().add(new Motions.Loader());
        loaderManager.getLoaders().add(new Collisions.Loader());
        loaderManager.getLoaders().add(new Constraints.Loader());
        loaderManager.getLoaders().add(new Aircrafts.AircraftLoader(loaderManager));
        loaderManager.getLoaders().add(new Aircrafts.AircraftControllerLoader());
        loaderManager.getLoaders().add(new Guns.GunLoader(loaderManager));
        loaderManager.getLoaders().add(new Guns.GunControllerLoader());
        loaderManager.getEnvironment().put("world", world);

        // ----- Create Environment ----- //
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.2f, -0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.2f, 0.8f, -1f));

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
                    Map config = loaderManager.getConfig(world, Map.class);
//                    Yaml yaml = new Yaml();
//                    String yamlConfig = yaml.dumpAsMap(config);
//                    System.out.println(yamlConfig);
//                    Map loadedConfig = yaml.loadAs(yamlConfig, Map.class);

                    LoaderManager loaderManager1 = new LoaderManager();
                    loaderManager1.getLoaders().add(new Motions.Loader());
                    loaderManager1.getLoaders().add(new Collisions.Loader());
                    loaderManager1.getLoaders().add(new Constraints.Loader());
                    loaderManager1.getLoaders().add(new Aircrafts.AircraftLoader(loaderManager1));
                    loaderManager1.getLoaders().add(new Aircrafts.AircraftControllerLoader());
                    loaderManager1.getLoaders().add(new Guns.GunLoader(loaderManager1));
                    loaderManager1.getLoaders().add(new Guns.GunControllerLoader());
                    loaderManager1.getLoader(WorldLoader.class).setBeforeLoadAssets(world1 -> {
                        AssetsManager assetsManager = world1.getAssetsManager();
                        MyGame.initAssets(assetsManager, skyModel);
                        SceneBuilder.initAssets(assetsManager);
                        Aircrafts.initAssets(assetsManager);
                        Guns.initAssets(assetsManager);
                        Scripts.initAssets(assetsManager);
                    });

                    world = loaderManager1.load(config, World.class);
                    renderSystem = world.getSystemManager().getSystem(RenderSystem.class);
                    physicsSystem = world.getSystemManager().getSystem(PhysicsSystem.class);
                    serializationSystem = world.getSystemManager().getSystem(SerializationSystem.class);
                    constraintSystem = world.getSystemManager().getSystem(ConstraintSystem.class);
                    motionSystem = world.getSystemManager().getSystem(MotionSystem.class);
                    scriptSystem = world.getSystemManager().getSystem(ScriptSystem.class);
                    loaderManager = loaderManager1;
                    Aircrafts.Aircraft aircraft = world.getEntityManager().getEntity("Aircraft-6").getComponent(Aircrafts.Aircraft.class);
                    Guns.Gun gun = world.getEntityManager().getEntity("Gun-0").getComponent(Guns.Gun.class);
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

        skyModel = assetManager.get("obj/sky.g3db", Model.class);
        skyModel.nodes.get(0).scale.scl(20);
        initAssets(world.getAssetsManager(), skyModel);

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
        for (int i = 0; i < 100; i++) {
            SceneBuilder.createBox(new Matrix4().translate(10, 0.5f, -10 * i), ground.getId());
            SceneBuilder.createBox(new Matrix4().translate(-10, 0.5f, -10 * i), ground.getId());
        }
        for (int i = 1; i < 5; i++) {
            SceneBuilder.createTower(new Matrix4().setToTranslation(-5, 0, -200 * i), 5 * i);
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
        Aircrafts.Aircraft aircraft = aircraftEntity.getComponent(Aircrafts.Aircraft.class);
        vehicles.add(aircraft);

        Entity gunEntity = gunBuilder.createGun("ground", new Matrix4().translate(0, 0, -20));
        ScriptComponent gunScriptComponent = new ScriptComponent();
        gunScriptComponent.script = world.getAssetsManager().getAsset("GunScript", ScriptSystem.Script.class);
        gunScriptComponent.disabled = true;
        gunEntity.addComponent(gunScriptComponent);
        Guns.Gun gun = gunEntity.getComponent(Guns.Gun.class);
        vehicles.add(gun);

        // ----- Init World & Constraint ----- //
        world.update();
        physicsSystem.update(0);
    }

    private static void initAssets(AssetsManager assetsManager, Model skyModel) {

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

    @Override
    protected void myRender() {
        // ----- Net----- //
        float deltaTime = 1 / 60f;
        if (server != null) {
            delayD += Gdx.graphics.getDeltaTime();
            if (delayD >= delay*0.001) {
                server.send(serializationSystem.serialize("serializable"));
                delayD = 0;
            }
        } else {
            if (receivedData != null) {
                serializationSystem.deserialize(receivedData);
                deltaTime = (System.currentTimeMillis() - receivedTime) * 0.001f;
                System.out.println("[" + deltaTime + "] Deserialize Received Data: " + receivedData.hashCode());
                receivedData = null;
                receivedTime = 0;
            }
        }

        // Render
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mainView.setCamera(camera);
        world.getEntityManager().getEntity("sky").getComponent(Position.class).transform.setToTranslation(camera.position);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        renderSystem.render(camera, environment);
        Gdx.gl.glViewport(0, Gdx.graphics.getHeight() - 250, 400, 250);
        secondaryView.setCamera(camera);
        world.getEntityManager().getEntity("sky").getComponent(Position.class).transform.setToTranslation(camera.position);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        renderSystem.render(camera, environment);

        // Update Info
        Aircrafts.Aircraft aircraft = (Aircrafts.Aircraft) vehicles.get(0);
        ui.getWidget("label", Label.class).setText(
                "  Velocity: " + Math.floor(aircraft.getVelocity()) +
                        "\n  Height: " + Math.floor(aircraft.getHeight()));

        // Update World
        world.update();
        constraintSystem.update();
        motionSystem.update();
        physicsSystem.update(deltaTime);
        scriptSystem.update();
        world.getEntityManager().getBatch().commit();

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
            return world.getEntityManager().getEntity(entityId);
        }
        private void changeCamera() {
            cameraIndex = (cameraIndex + 1) % 2;
        }
        private void changeVehicle() {
            vehicleIndex = (vehicleIndex + 1) % vehicles.size;
        }
    }
}
