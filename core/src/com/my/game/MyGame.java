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
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.utils.base.Base3DGame;
import com.my.utils.net.Client;
import com.my.utils.net.Server;
import com.my.utils.world.World;
import com.my.utils.world.com.Position;
import com.my.utils.world.com.Render;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.com.Serialization;
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
    private ObjectBuilder objectBuilder;
    private String aircraft;
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
        objectBuilder = new ObjectBuilder(world);

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
                if (keycode == Input.Keys.V) addBomb();
                if (keycode == Input.Keys.SPACE) explode();
                if (keycode == Input.Keys.TAB) changeCamera();
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
                    explode();
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
        models.put("ground", mdBuilder.createBox(100f, 0.01f, 2000f, new Material(ColorAttribute.createDiffuse(Color.WHITE)), attributes));
        models.put("box", mdBuilder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.RED)), attributes));
        models.put("box1", mdBuilder.createBox(2, 1, 1, new Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY)), attributes));
        models.put("bomb", mdBuilder.createCapsule(0.5f, 2, 8, new Material(ColorAttribute.createDiffuse(Color.GRAY)), attributes));
        models.put("body", mdBuilder.createBox(1, 1, 5, new Material(ColorAttribute.createDiffuse(Color.GREEN)), attributes));
        models.put("wing", mdBuilder.createBox(2, 0.2f, 1, new Material(ColorAttribute.createDiffuse(Color.BLUE)), attributes));
        models.put("rotate", mdBuilder.createCylinder(1, 1, 1, 8, new Material(ColorAttribute.createDiffuse(Color.CYAN)), attributes));
        models.put("engine", mdBuilder.createCone(0.9f, 1, 0.9f, 18, new Material(ColorAttribute.createDiffuse(Color.YELLOW)), attributes));

        // ----- Init Configs ----- //
        Render.addConfig("sky", new Render.Config(models.get("sky"), false));
        Render.addConfig("ground", new Render.Config(models.get("ground")));
        Render.addConfig("box", new Render.Config(models.get("box")));
        Render.addConfig("box1", new Render.Config(models.get("box1")));
        Render.addConfig("bomb", new Render.Config(models.get("bomb")));
        Render.addConfig("body", new Render.Config(models.get("body")));
        Render.addConfig("wing", new Render.Config(models.get("wing")));
        Render.addConfig("rotate", new Render.Config(models.get("rotate")));
        Render.addConfig("engine", new Render.Config(models.get("engine")));

        RigidBody.addConfig("ground", new RigidBody.Config(new btBoxShape(new Vector3(50,0.005f,1000)), 0f));
        RigidBody.addConfig("box", new RigidBody.Config(new btBoxShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        RigidBody.addConfig("box1", new RigidBody.Config(new btBoxShape(new Vector3(1,0.5f,0.5f)), 50f));
        RigidBody.addConfig("bomb", new RigidBody.Config(new btCapsuleShape(0.5f, 1), 50f));
        RigidBody.addConfig("body", new RigidBody.Config(new btBoxShape(new Vector3(0.5f,0.5f,2.5f)), 50f));
        RigidBody.addConfig("wing", new RigidBody.Config(new btBoxShape(new Vector3(1f,0.1f,0.5f)), 25f));
        RigidBody.addConfig("rotate", new RigidBody.Config(new btCylinderShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        RigidBody.addConfig("engine", new RigidBody.Config(new btConeShape(0.45f,1), 50));

        Serialization.Serializer serializer = new Serializer(world);
        Serialization.addSerializer("box", serializer);

        // ----- Init Static Objects ----- //
        world.addEntity("sky", new MyInstance("sky"));
        world.addEntity("ground", new MyInstance("ground"));

        // ----- Init Dynamic Objects ----- //
        for (int i = 0; i < 100; i++) {
            objectBuilder.createBox(new Matrix4().translate(10, 0.5f, -10 * i), null);
            objectBuilder.createBox(new Matrix4().translate(-10, 0.5f, -10 * i), null);
        }
        for (int i = 1; i < 5; i++) {
            objectBuilder.createTower(new Matrix4().setToTranslation(-5, 0, -200 * i), 5 * i);
        }
        aircraft = objectBuilder.createAircraft(new Matrix4().translate(0, 0, 200), 8000, 40, Input.Keys.UP, Input.Keys.DOWN, Input.Keys.LEFT, Input.Keys.RIGHT);
        objectBuilder.createAircraft(new Matrix4().translate(20, 0, 0), 4000, 40, 0,0,0,0);
        objectBuilder.createAircraft(new Matrix4().translate(-20, 0, 0), 4000, 40, 0,0,0,0);
        objectBuilder.createAircraft(new Matrix4().translate(20, 20, 0), 4000, 40, 0,0,0,0);
        objectBuilder.createAircraft(new Matrix4().translate(-20, 20, 0), 4000, 40, 0,0,0,0);
        objectBuilder.createAircraft(new Matrix4().translate(20, 0, 20), 4000, 40, 0,0,0,0);
        objectBuilder.createAircraft(new Matrix4().translate(-20, 0, 20), 4000, 40, 0,0,0,0);
        objectBuilder.createAircraft(new Matrix4().translate(20, 0, -20), 4000, 40, 0,0,0,0);
        objectBuilder.createAircraft(new Matrix4().translate(-20, 0, -20), 4000, 40, 0,0,0,0);

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
        if (firstPerson) render_FirstPerson(); else render_ThirdPerson();
        Gdx.gl.glViewport(0, Gdx.graphics.getHeight() - 250, 400, 250);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
        if (firstPerson) render_ThirdPerson(); else render_FirstPerson();

        // Update Info
        double velocity = world.getEntity(aircraft).get(RigidBody.class).body.getLinearVelocity().len();
        double height = world.getEntity(aircraft).get(Position.class).transform.getTranslation(tmpV).y;
        velocity = Math.floor(velocity);
        height = Math.floor(height);
        ui.getWidget("label", Label.class).setText(
                "Velocity: " + velocity +
                        "\nHeight: " + height);

        // Update World
        world.update();
        constraintSystem.update();
        motionSystem.update();
        physicsSystem.update(deltaTime);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private boolean firstPerson = true;
    private void render_FirstPerson() {
        // Update Camera
        Matrix4 transform = world.getEntity(aircraft).get(Position.class).transform;
        camera.position.set(0, 0, -2.5f).mul(transform);
        camera.direction.set(0, 0, -1).rot(transform);
        camera.up.set(0, 1 , 0).rot(transform);
        camera.update();
        // Render
        renderSystem.render(camera, environment);
//        physicsSystem.renderDebug(camera);
    }

    private void render_ThirdPerson() {
        // Update Camera
        Matrix4 transform = world.getEntity(aircraft).get(Position.class).transform;
        transform.getTranslation(tmpV);
        float angle = transform.getRotation(tmpQ).getAngleAround(Vector3.Y);
        tmpM.setToTranslation(tmpV).rotate(Vector3.Y, angle).translate(0, 0, 20);
        camera.position.setZero().mul(tmpM);
        camera.lookAt(transform.getTranslation(tmpV).add(0, 0, 0));
        camera.up.set(0, 1, 0);
        camera.update();
        world.getEntity("sky").get(Position.class).transform.setToTranslation(camera.position);
        // Render
        renderSystem.render(camera, environment);
//        physicsSystem.renderDebug(camera);
    }

    // ----- Custom ----- //
    private void addBomb() {
        tmpM.set(world.getEntity(aircraft).get(Position.class).transform)
                .translate(0, 0, -6.5f)
                .rotate(Vector3.X, 90);
        world.getEntity(aircraft).get(Position.class).transform.getRotation(tmpQ);
        tmpV.set(world.getEntity(aircraft).get(RigidBody.class).body.getLinearVelocity());
        tmpV.add(new Vector3(0, 0, -1).mul(tmpQ).scl(2000));
        btRigidBody body = objectBuilder.createBomb(tmpM, null)
                .get(RigidBody.class).body;
        body.setLinearVelocity(tmpV);
        body.setCcdMotionThreshold(1e-7f);
        body.setCcdSweptSphereRadius(2);
    }
    private void explode() {
        System.out.println("Explosion!");
        constraintSystem.clear(world);
        physicsSystem.addExplosion(world.getEntity(aircraft).get(Position.class).transform.getTranslation(tmpV), 2000);
    }
    private void changeCamera() {
        firstPerson = !firstPerson;
    }
}
