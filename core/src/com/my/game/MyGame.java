package com.my.game;

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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.utils.base.Base3DGame;
import com.my.utils.net.Client;
import com.my.utils.net.Server;
import com.my.utils.world.World;
import com.my.utils.world.mod.*;

import java.net.SocketException;
import java.net.UnknownHostException;

public class MyGame extends Base3DGame {

    private Environment environment;
    private World world;
    private ModelModule modelHandler;
    private PhyModule phyHandler;
    private SerializeModule serializeModule;
    private ArrayMap<String, Model> models = new ArrayMap<>();
    private Client client;
    private Server server;
    @Override
    public void create() {
        super.create();

        // ----- Init World ----- //
        Bullet.init();
        world = new World();
        // Create modelHandler
        modelHandler = new ModelModule();
        world.addModule("model", modelHandler);
        addDisposable(modelHandler);
        // Create phyHandler
        phyHandler = new PhyModule();
        world.addModule("phy", phyHandler);
        addDisposable(phyHandler);
        // Create phyHandler
        serializeModule = new SerializeModule();
        world.addModule("serialize", serializeModule);
        addDisposable(serializeModule);

        // ----- Create Environment ----- //
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.2f, -0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.2f, 0.8f, -1f));

        // ----- Loading Assets ----- //
        assetManager.load("obj/sky.g3db", Model.class);
        waitLoad(true);

        // ----- Loading Assets ----- //
        try {
            client = new Client("127.0.0.1", 1002, this::receive);
        } catch (SocketException e) {
            try {
                server = new Server("127.0.0.1", 1002);
                new Thread(() -> {
                    while (true) {
                        int delay = (int) ui.getWidget("slider", Slider.class).getValue();
                        try { Thread.sleep(delay); } catch (InterruptedException ignored) {}
                        send();
                    }
                }).start();
            } catch (SocketException | UnknownHostException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    @Override
    protected void initUI() {
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

        // Add btnSend
        TextButton btnSend = new TextButton("Send!", ui.skin);
        btnSend.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                send();
            }
        });
        ui.addWidget("btnSend", btnSend);

        // Add Slider
        Slider slider = new Slider(0, 1000, 10, false, ui.skin);
        ui.addWidget("slider", slider);

        // Add Label
        Label label = new Label("", ui.skin);
        label.setWrap(true);
//        label.getStyle().fontColor.set(1, 1, 1, 1);
        ui.addWidget("label", label);

        inputMultiplexer.addProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.F) addBox();
                return false;
            }
        });
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
        ModelComponent.addConfig("sky", new ModelComponent.Config(models.get("sky"), false));
        ModelComponent.addConfig("ground", new ModelComponent.Config(models.get("ground")));
        ModelComponent.addConfig("box1", new ModelComponent.Config(models.get("box1")));
        ModelComponent.addConfig("box2", new ModelComponent.Config(models.get("box2")));
        ModelComponent.addConfig("box3", new ModelComponent.Config(models.get("box3")));

        PhyComponent.addConfig("ground", new PhyComponent.Config(new btBoxShape(new Vector3(100,0.005f,100)), 0f));
        PhyComponent.addConfig("box1", new PhyComponent.Config(new btBoxShape(new Vector3(0.5f,0.5f,0.5f)), 50f));
        PhyComponent.addConfig("box2", new PhyComponent.Config(new btBoxShape(new Vector3(0.5f,1f,0.5f)), 50f));
        PhyComponent.addConfig("box3", new PhyComponent.Config(new btBoxShape(new Vector3(1f,0.5f,0.5f)), 50f));

        // ----- Init Objects ----- //
        world.addInstance("sky", new MyInstance("sky"));
        world.addInstance("ground", new MyInstance("ground"));
    }

    @Override
    protected void myRender() {
        // Update Camera
        cameraControllerMultiplexer.update();
        ui.getWidget("label", Label.class).setText("Delay: " + (int) ui.getWidget("slider", Slider.class).getValue());
        // Update Handlers
        phyHandler.update();
        // Render
        modelHandler.render(cam, environment);
        phyHandler.renderDebug(cam);
    }

    // ----- Custom----- //
    private int num = 0;
    private void addBox() {
        String type = (String) ui.getWidget("list", List.class).getSelected();
        String name = type + "-" + num++;
        System.out.println("Add: " + name);
        world.addInstance(name, new SerializeInstance(type, name));
    }

    // ----- Net----- //
    private void send() {
        if (server != null) {
            String data = serializeModule.serialize();
            System.out.println("Send Data: " + data.hashCode());
            server.send(data);
        }
    }
    private void receive(String data) {
        if (data != null) {
            System.out.println("Receive Data: " + data.hashCode());
            serializeModule.deserialize(data);
        }
    }
}
