package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.utils.base.Base3DGame;
import com.my.utils.net.Client;
import com.my.utils.net.Server;
import com.my.utils.world.World;
import com.my.utils.world.com.Render;
import com.my.utils.world.com.RigidBody;
import com.my.utils.world.com.Serialization;
import com.my.utils.world.sys.PhysicsSystem;
import com.my.utils.world.sys.RenderSystem;
import com.my.utils.world.sys.SerializationSystem;

import java.net.SocketException;
import java.net.UnknownHostException;

public class MyGame extends Base3DGame {

    private Environment environment;
    private World world;
    private RenderSystem renderSystem;
    private PhysicsSystem physicsSystem;
    private SerializationSystem serializationSystem;
    private ArrayMap<String, Model> models = new ArrayMap<>();
    private Server server;
    private String data = null;
    private int delay;
    private float delayD;
    @Override
    public void create() {
        // ----- Net ----- //
        try {
            server = new Server("127.0.0.1", 1001, "127.0.0.1", 1002);
        } catch (SocketException | UnknownHostException e) {
            try {
                Client client = new Client("127.0.0.1", 1002, (data) -> {
                    System.out.println("Receive Data: " + data.hashCode());
                    MyGame.this.data = data;
                });
            } catch (SocketException e1) {
                throw new RuntimeException(e1);
            }
        }

        super.create();

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
                if (keycode == Input.Keys.F) addBox();
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

            // Add List
            List<String> list = new List<>(ui.skin);
            list.setItems("box1", "box2", "box3");
            ui.addWidget("list", list);

            // Add btnAddBox
            TextButton btnAddBox = new TextButton("Add Box!", ui.skin);
            btnAddBox.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    addBox();
                }
            });
            ui.addWidget("btnAddBox", btnAddBox);

            // Add Slider
            Slider slider = new Slider(0, 1000, 10, false, ui.skin);
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
        models.put("box1", mdBuilder.createBox(1, 1, 1, new Material(ColorAttribute.createDiffuse(Color.RED)), attributes));
        models.put("box2", mdBuilder.createBox(1, 2, 1, new Material(ColorAttribute.createDiffuse(Color.YELLOW)), attributes));
        models.put("box3", mdBuilder.createBox(2, 1, 1, new Material(ColorAttribute.createDiffuse(Color.BLUE)), attributes));

        // ----- Init Configs ----- //
        Render.addConfig("sky", new Render.Config(models.get("sky"), false));
        Render.addConfig("ground", new Render.Config(models.get("ground")));
        Render.addConfig("box1", new Render.Config(models.get("box1")));
        Render.addConfig("box2", new Render.Config(models.get("box2")));
        Render.addConfig("box3", new Render.Config(models.get("box3")));

        RigidBody.addConfig("ground", new RigidBody.Config(new btBoxShape(new Vector3(100,0.005f,100)), 0f));
        RigidBody.addConfig("box1", new RigidBody.Config(new btBoxShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        RigidBody.addConfig("box2", new RigidBody.Config(new btBoxShape(new Vector3(0.5f,1f,0.5f)), 50f));
        RigidBody.addConfig("box3", new RigidBody.Config(new btBoxShape(new Vector3(1f,0.5f,0.5f)), 50f));

        Serialization.Serializer serializer = new Serializer(world);
        Serialization.addSerializer("box1", serializer);
        Serialization.addSerializer("box2", serializer);
        Serialization.addSerializer("box3", serializer);

        // ----- Init Objects ----- //
        world.addEntity("sky", new MyInstance("sky"));
        world.addEntity("ground", new MyInstance("ground"));
    }

    @Override
    protected void myRender() {
        // ----- Net----- //
        if (server != null) {
            delayD += Gdx.graphics.getDeltaTime();
            if (delayD >= delay*0.001) {
                server.send(serializationSystem.serialize("box"));
                delayD = 0;
            }
        } else {
            if (data != null) {
                serializationSystem.deserialize(data);
                data = null;
            }
        }
        // Update Camera
        cameraControllerMultiplexer.update();
        // Update World
        world.update();
        // Render
        physicsSystem.update();
        renderSystem.render(cam, environment);
        physicsSystem.renderDebug(cam);
    }

    // ----- Custom----- //
    private int num = 0;
    private void addBox() {
        String type = (String) ui.getWidget("list", List.class).getSelected();
        String name = type + "-" + num++;
        System.out.println("Add: " + name);
        world.addEntity(name, new MyInstance(type, "box"));
    }
}
