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
import com.my.utils.world.World;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Render;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.sys.*;

import java.net.SocketException;
import java.net.UnknownHostException;

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
    private ArrayMap<String, Model> models = new ArrayMap<>();
    private Array<Controllable> vehicles = new Array<>();
    private Server server;
    private String receivedData = null;
    private long receivedTime;
    private int delay = 500;
    private float delayD;
    private PerspectiveCamera camera;
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
        renderSystem = world.addSystem(RenderSystem.class, new RenderSystem());
        addDisposable(renderSystem);
        // Create physicsSystem
        physicsSystem = world.addSystem(PhysicsSystem.class, new PhysicsSystem());
        addDisposable(physicsSystem);
        // Create serializationSystem
        serializationSystem = world.addSystem(SerializationSystem.class, new SerializationSystem());
        addDisposable(serializationSystem);
        // Create constraintSystem
        constraintSystem = world.addSystem(ConstraintSystem.class, new ConstraintSystem());
        addDisposable(constraintSystem);
        // Create MotionSystem
        motionSystem = world.addSystem(MotionSystem.class, new MotionSystem());
        addDisposable(motionSystem);
        // Create ObjectBuilder
        SceneBuilder.init(world);
        AircraftBuilder.init(world);
        GunBuilder.init(world);

        // ----- Create Environment ----- //
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
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
                if (keycode == Input.Keys.V) mainView.getVehicle().fire();
                if (keycode == Input.Keys.SPACE) mainView.getVehicle().explode();
                if (keycode == Input.Keys.TAB) changeView();
                if (keycode == Input.Keys.SHIFT_LEFT) mainView.changeCamera();
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
                    mainView.getVehicle().explode();
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

        // ----- Init Models ----- //
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();
        models.put("sky", assetManager.get("obj/sky.g3db", Model.class));
        models.get("sky").nodes.get(0).scale.scl(20);
        models.put("ground", mdBuilder.createBox(10000f, 0.01f, 20000f, new Material(ColorAttribute.createDiffuse(Color.WHITE)), attributes));

        // ----- Init Configs ----- //
        Render.addConfig("sky", new Render.Config(models.get("sky"), false));
        Render.addConfig("ground", new Render.Config(models.get("ground")));

        RigidBody.addConfig("ground", new RigidBody.Config(new btBoxShape(new Vector3(5000,0.005f,10000)), 0f));

        // ----- Init Static Objects ----- //
        world.addEntity("sky", new MyInstance("sky"));
        world.addEntity("ground", new MyInstance("ground"));

        // ----- Init Dynamic Objects ----- //
        for (int i = 0; i < 100; i++) {
            SceneBuilder.createBox(new Matrix4().translate(10, 0.5f, -10 * i), null);
            SceneBuilder.createBox(new Matrix4().translate(-10, 0.5f, -10 * i), null);
        }
        for (int i = 1; i < 5; i++) {
            SceneBuilder.createTower(new Matrix4().setToTranslation(-5, 0, -200 * i), 5 * i);
        }
        for (int x = -20; x <= 20; x+=40) {
            for (int y = 0; y <= 0; y+=20) {
                for (int z = -20; z <= 20; z+=20) {
                    AircraftBuilder.createAircraft(new Matrix4().translate(x, y, z), 4000, 40);
                }
            }
        }
        vehicles.add(AircraftBuilder.createAircraft(new Matrix4().translate(0, 0, 200), 8000, 40));
        vehicles.add(GunBuilder.createGun(new Matrix4().translate(0, 0, -20)));

        // ----- Init World & Constraint ----- //
        world.update();
        physicsSystem.update(0);
        constraintSystem.init(world);
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
        world.getEntity("sky").get(Position.class).transform.setToTranslation(camera.position);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        renderSystem.render(camera, environment);
        Gdx.gl.glViewport(0, Gdx.graphics.getHeight() - 250, 400, 250);
        secondaryView.setCamera(camera);
        world.getEntity("sky").get(Position.class).transform.setToTranslation(camera.position);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        renderSystem.render(camera, environment);

        // Update Info
        AircraftBuilder.Aircraft aircraft = (AircraftBuilder.Aircraft) vehicles.get(0);
        ui.getWidget("label", Label.class).setText(
                "Velocity: " + Math.floor(aircraft.getVelocity()) +
                        "\nHeight: " + Math.floor(aircraft.getHeight()));

        // Update World
        world.update();
        constraintSystem.update();
        motionSystem.update();
        physicsSystem.update(deltaTime);
        mainView.getVehicle().update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    // ----- Custom ----- //
    private View mainView = new View(0, 0);
    private View secondaryView = new View(1, 0);
    private void changeView() {
        View tmp = mainView;
        mainView = secondaryView;
        secondaryView = tmp;
    }

    private class View {
        int cameraIndex = 0;
        int vehicleIndex = 0;
        private View(int vehicleIndex, int cameraIndex) {
            this.vehicleIndex = vehicleIndex;
            this.cameraIndex = cameraIndex;
        }
        private void setCamera(PerspectiveCamera camera) {
            vehicles.get(vehicleIndex).setCamera(camera, cameraIndex);
        }
        private Controllable getVehicle() {
            return vehicles.get(vehicleIndex);
        }
        private void changeCamera() {
            cameraIndex = (cameraIndex + 1) % 2;
        }
        private void changeVehicle() {
            vehicleIndex = (vehicleIndex + 1) % vehicles.size;
        }
    }
}
