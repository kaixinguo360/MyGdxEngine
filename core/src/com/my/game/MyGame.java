package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.dynamics.btHingeConstraint;
import com.badlogic.gdx.physics.bullet.dynamics.btTypedConstraint;
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
import com.my.utils.world.Entity;
import com.my.utils.world.World;
import com.my.utils.world.com.*;
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
                if (keycode == Input.Keys.ESCAPE) Gdx.app.exit();
                if (keycode == Input.Keys.V) addBomb();
                if (keycode == Input.Keys.SPACE) explode();
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
        models.put("ground", mdBuilder.createBox(100f, 0.01f, 2000f, new Material(ColorAttribute.createDiffuse(Color.WHITE)), attributes));
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

        RigidBody.addConfig("ground", new RigidBody.Config(new btBoxShape(new Vector3(50,0.005f,1000)), 0f));
        RigidBody.addConfig("box", new RigidBody.Config(new btBoxShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        RigidBody.addConfig("body", new RigidBody.Config(new btBoxShape(new Vector3(0.5f,0.5f,2.5f)), 250f));
        RigidBody.addConfig("wing", new RigidBody.Config(new btBoxShape(new Vector3(1f,0.1f,0.5f)), 20f));
        RigidBody.addConfig("engine", new RigidBody.Config(new btConeShape(0.45f,1), 50));

        Serialization.Serializer serializer = new Serializer(world);
        Serialization.addSerializer("box", serializer);

        // ----- Init Static Objects ----- //
        world.addEntity("sky", new MyInstance("sky"));
        world.addEntity("ground", new MyInstance("ground"));
        for (int i = 0; i < 100; i++) {
            addBox(new Matrix4().translate(10, 0.5f, -10 * i), null);
            addBox(new Matrix4().translate(-10, 0.5f, -10 * i), null);
        }

        // ----- Init Aircraft ----- //
        world.addEntity("base", new MyInstance("body", group))
                .get(Position.class).transform.translate(0, 0.5f, -3);
        // Tail
        addRotateWing(new Matrix4().translate(1, 0.5f, 0.1f).rotate(Vector3.X, 15.3f),
                new Controller(-0.15f, 0.15f, 1f, Input.Keys.DOWN, Input.Keys.UP), "base");
        addRotateWing(new Matrix4().translate(-1, 0.5f, 0.1f).rotate(Vector3.X, 15.3f),
                new Controller(-0.15f, 0.15f, 1f, Input.Keys.DOWN, Input.Keys.UP), "base");

        // Left
        addRotateWing(new Matrix4().translate(-1.5f, 0.5f, -5).rotate(Vector3.X, 15),
                new Controller(-0.1f, 0.1f, 0.005f, Input.Keys.RIGHT, Input.Keys.LEFT), "base");
        addRotateWing(new Matrix4().translate(-3.5f, 0.5f, -5).rotate(Vector3.X, 15),
                new Controller(-0.1f, 0.1f, 0.005f, Input.Keys.RIGHT, Input.Keys.LEFT), "base");

        // Right
        addRotateWing(new Matrix4().translate(1.5f, 0.5f, -5).rotate(Vector3.X, 15),
                new Controller(-0.1f, 0.1f, 0.005f, Input.Keys.LEFT, Input.Keys.RIGHT), "base");
        addRotateWing(new Matrix4().translate(3.5f, 0.5f, -5).rotate(Vector3.X, 15),
                new Controller(-0.1f, 0.1f, 0.005f, Input.Keys.LEFT, Input.Keys.RIGHT), "base");

        // Vertical
        addWing(new Matrix4().translate(0.6f, 1f, -1).rotate(Vector3.Z, 90), "base");
        addWing(new Matrix4().translate(-0.6f, 1f, -1).rotate(Vector3.Z, 90), "base");
        addEngine(new Matrix4().translate(0, 0.6f, -6).rotate(Vector3.X, -90), "base");

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
        // Update Camera
        Matrix4 transform = world.getEntity("base").get(Position.class).transform;
        transform.getTranslation(tmpV);
        float angle = transform.getRotation(tmpQ).getAngleAround(Vector3.Y);
        tmpM.setToTranslation(tmpV).rotate(Vector3.Y, angle).translate(0, 10, 20);
        camera.position.setZero().mul(tmpM);
        camera.lookAt(transform.getTranslation(tmpV));
        camera.up.set(0, 1, 0);
        camera.update();
        world.getEntity("sky").get(Position.class).transform.setToTranslation(camera.position);
        // Update Info
        double velocity = world.getEntity("base").get(RigidBody.class).body.getLinearVelocity().len();
        double height = world.getEntity("base").get(Position.class).transform.getTranslation(tmpV).y;
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
        // Render
        renderSystem.render(camera, environment);
//        physicsSystem.renderDebug(camera);
    }

    // ----- Custom ----- //
    private String group = "group";
    private int box = 0;
    private Entity addBox(Matrix4 transform, String base) {
        Entity entity = new MyInstance("box", "box");
        addObject(
                "Box-" + box++,
                transform,
                entity,
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
        return entity;
    }
    private int body = 0;
    private String addBody(Matrix4 transform, String base) {
        return addObject(
                "Body-" + body++,
                transform,
                new MyInstance("body", group),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
    }
    private int wing = 0;
    private String addWing(Matrix4 transform, String base) {
        return addObject(
                "Wing-" + wing++,
                transform,
                new MyInstance("wing", group, new Motion.Lift(new Vector3(0, 120, 0))),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
    }
    private int rotate = 0;
    private String addRotateWing(Matrix4 transform, ConstraintSystem.Controller controller, String base) {
        Matrix4 relTransform = new Matrix4(world.getEntity(base).get(Position.class).transform).inv().mul(transform);
        String id = addObject(
                "Rotate-" + rotate++,
                transform,
                new MyInstance("wing", group, new Motion.Lift(new Vector3(0, 120, 0))),
                base,
                base == null ? null : new ConstraintSystem.HingeConstraint(
                        relTransform.rotate(Vector3.Y, 90),
                        new Matrix4().rotate(Vector3.Y, 90),
                        false)
        );
        constraintSystem.addController(id, base, controller);
        return id;
    }
    private int engine = 0;
    private String addEngine(Matrix4 transform, String base) {
        return addObject(
                "Engine-" + engine++,
                transform,
                new MyInstance("engine", group, new Motion.LimitedForce(40, new Vector3(0, 3000, 0))),
                base,
                base == null ? null : new ConstraintSystem.ConnectConstraint()
        );
    }

    private class Controller implements ConstraintSystem.Controller {
        private float low;
        private float high;
        private float delta;
        private int down;
        private int up;
        private float target = 0;
        private Controller(float low, float high, float delta, int down, int up) {
            this.low = low;
            this.high = high;
            this.delta = delta;
            this.down = down;
            this.up = up;
        }
        @Override
        public void update(btTypedConstraint constraint) {
            if (Gdx.input.isKeyPressed(up)) {
                target += delta;
            } else if (Gdx.input.isKeyPressed(down)) {
                target -= delta;
            } else {
                target += target > 0 ? -delta : (target < 0 ? delta : 0);
            }
            target = target > high ? high : (target < low ? low : target);
            btHingeConstraint hingeConstraint = (btHingeConstraint) constraint;
            hingeConstraint.setLimit(target, target, 0, 0.5f);
        }
    }
    private String addObject(String id, Matrix4 transform, Entity entity, String base, ConstraintSystem.Config constraint) {
        world.addEntity(id, entity)
                .get(Position.class).transform.set(transform);
        if (base != null) constraintSystem.addConstraint(base, id, constraint);
        return id;
    }

    private void addBomb() {
        System.out.println("Bomb!");
        tmpM.set(world.getEntity("base").get(Position.class).transform).translate(0, -1, 0);
        addBox(tmpM, null).get(RigidBody.class).body.setLinearVelocity(
                world.getEntity("base").get(RigidBody.class).body.getLinearVelocity());
    }
    private void explode() {
        System.out.println("Explosion!");
        constraintSystem.clear(world);
    }
}
