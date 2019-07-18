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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.utils.base.Base3DGame;
import com.my.utils.world.World;
import com.my.utils.world.mod.ModelComponent;
import com.my.utils.world.mod.ModelHandler;
import com.my.utils.world.mod.PhyComponent;
import com.my.utils.world.mod.PhyHandler;

public class MyGame extends Base3DGame {

    private Environment environment;
    private World world;
    private ModelHandler modelHandler;
    private PhyHandler phyHandler;
    private ArrayMap<String, Model> models = new ArrayMap<>();
    @Override
    public void create() {
        super.create();

        // ----- Init World ----- //
        Bullet.init();
        world = new World();
        // Create modelHandler
        modelHandler = new ModelHandler();
        world.addHandler("model", modelHandler);
        addDisposable(modelHandler);
        // Create phyHandler
        phyHandler = new PhyHandler();
        world.addHandler("phy", phyHandler);
        addDisposable(phyHandler);

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
        // Add List
        List<String> list = new List<>(ui.skin);
        list.setItems("box1", "box2", "box3");
        list.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Selected: " + ((List) actor).getSelected());
            }
        });
        ui.addWidget("list", list);

        // Add Button3
        TextButton button = new TextButton("Add Box!", ui.skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                addBox();
            }
        });
        ui.addWidget("button3", button);

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
        ui.getWidget("label", Label.class).setText("Cam: " + cameraControllerMultiplexer.getActivatedCameraName());
        // Update Handlers
        phyHandler.update();
        // Render
        modelHandler.render(cam, environment);
//        phyHandler.renderDebug(cam);
    }

    private int num = 0;
    private void addBox() {
        String type = (String) ui.getWidget("list", List.class).getSelected();
        String name = type + "-" + num++;
        System.out.println("Add: " + name);
        world.addInstance(name, new MyInstance(type));
    }
}
