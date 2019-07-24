package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.utils.base.Base3DGame;
import com.my.utils.net.Client;
import com.my.utils.net.Server;
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.*;
import com.my.utils.world.sys.*;

import java.net.SocketException;
import java.net.UnknownHostException;

public class MyGame extends Base3DGame {

    private Environment environment;
    private World world;
    private RenderSystem renderSystem;
    private PhysicsSystem physicsSystem;
    private SerializationSystem serializationSystem;
    private ConstraintSystem constraintSystem;
    private MotionSystem motionSystem;
    private ArrayMap<String, Model> models = new ArrayMap<>();
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
        camera.far = 200;
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
        models.put("ground", mdBuilder.createBox(200f, 0.01f, 200f, new Material(ColorAttribute.createDiffuse(Color.WHITE)), attributes));
        models.put("box", mdBuilder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.RED)), attributes));
        models.put("body", mdBuilder.createBox(1, 1, 5, new Material(ColorAttribute.createDiffuse(Color.GREEN)), attributes));
        models.put("wing", mdBuilder.createBox(2, 0.2f, 1, new Material(ColorAttribute.createDiffuse(Color.BLUE)), attributes));
        models.put("engine", mdBuilder.createCone(0.9f, 1, 0.9f, 18, new Material(ColorAttribute.createDiffuse(Color.YELLOW)), attributes));

        // ----- Init Configs ----- //
        Render.addConfig("sky", new Render.Config(models.get("sky"), false));
        Render.addConfig("ground", new Render.Config(models.get("ground")));
        Render.addConfig("box", new Render.Config(models.get("box")));
        Render.addConfig("body", new Render.Config(models.get("body")));
        Render.addConfig("wing", new Render.Config(models.get("wing")));
        Render.addConfig("engine", new Render.Config(models.get("engine")));

        RigidBody.addConfig("ground", new RigidBody.Config(new btBoxShape(new Vector3(100,0.005f,100)), 0f));
        RigidBody.addConfig("box", new RigidBody.Config(new btBoxShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        RigidBody.addConfig("body", new RigidBody.Config(new btBoxShape(new Vector3(0.5f,0.5f,2.5f)), 250f));
        RigidBody.addConfig("wing", new RigidBody.Config(new btBoxShape(new Vector3(1f,0.1f,0.5f)), 20f));
        RigidBody.addConfig("engine", new RigidBody.Config(new btConeShape(0.45f,1), 50));

        Serialization.Serializer serializer = new Serializer(world);
        Serialization.addSerializer("box", serializer);
        Serialization.addSerializer("body", serializer);
        Serialization.addSerializer("wing", serializer);
        Serialization.addSerializer("engine", serializer);

        // ----- Init Objects ----- //
        world.addEntity("sky", new MyInstance("sky"));
        world.addEntity("ground", new MyInstance("ground"));

        world.addEntity("base", new MyInstance("body", group))
                .get(Position.class).transform.translate(0, 0.5f, -3);
        addWing(new Matrix4().translate(0, 0.5f, 0).rotate(Vector3.X, 15f), false);
        addWing(new Matrix4().translate(1.5f, 0.5f, -5).rotate(Vector3.X, 15), false);
        addWing(new Matrix4().translate(-1.5f, 0.5f, -5).rotate(Vector3.X, 15), false);
        addWing(new Matrix4().translate(0.6f, 1.1f, -1).rotate(Vector3.Z, 90), false);
        addWing(new Matrix4().translate(-0.6f, 1.1f, -1).rotate(Vector3.Z, 90), false);
        addEngine(new Matrix4().translate(0, 0.6f, -6).rotate(Vector3.X, -90), false);
        for (int i = 0; i < 10; i++) {
            addBox(new Matrix4().translate(5, 0.5f, -10 * i));
            addBox(new Matrix4().translate(-5, 0.5f, -10 * i));
        }

        // ----- Init World ----- //
        world.update();
        physicsSystem.update(0);

        // ----- Init Constraint ----- //
        constraintSystem.init(world);
    }

    @Override
    protected void myRender() {
        // ----- Net----- //
        float deltaTime = 1 / 60f;
        if (server != null) {
            delayD += Gdx.graphics.getDeltaTime();
            if (delayD >= delay*0.001) {
                server.send(serializationSystem.serialize("box"));
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
        // Update Camera
        cameraControllerMultiplexer.update();
        Matrix4 transform = world.getEntity("base").get(Position.class).transform;
        transform.getTranslation(camera.position);
        camera.position.add(cam.position);
        camera.direction.set(cam.direction);
        camera.update();
        world.getEntity("sky").get(Position.class).transform.setToTranslation(camera.position);
        // Update World
        world.update();
        motionSystem.update();
        physicsSystem.update(deltaTime);
        // Render
        renderSystem.render(camera, environment);
        physicsSystem.renderDebug(camera);
    }

    // ----- Add Object ----- //
    private String group = "group";
    private String last = "base";
    private int box = 0;
    private void addBox(Matrix4 transform) {
        world.addEntity("Box-" + box++, new MyInstance("box", "box"))
                .get(Position.class).transform.set(transform);
    }
    private int body = 0;
    private void addBody(Matrix4 transform, boolean updateLast) {
        addObject(
                "Body-" + body++,
                transform,
                new MyInstance("body", group),
                updateLast
        );
    }
    private int wing = 0;
    private void addWing(Matrix4 transform, boolean updateLast) {
        addObject(
                "Wing-" + wing++,
                transform,
                new MyInstance("wing", group, new Motion.Lift(new Vector3(0, 500, 0))),
                updateLast
        );
    }
    private int engine = 0;
    private void addEngine(Matrix4 transform, boolean updateLast) {
        addObject(
                "Engine-" + engine++,
                transform,
                new MyInstance("engine", group, new Motion.Force(new Vector3(0, 2000, 0))),
                updateLast
        );
    }
    private void addObject(String id, Matrix4 transform, Entity entity, boolean updateLast) {
        world.addEntity(id, entity)
                .get(Position.class).transform.set(transform);;
        constraintSystem.add(last, id, new ConstraintSystem.ConnectConstraint());
        if (updateLast) last = id;
    }
}
