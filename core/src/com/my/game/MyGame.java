package com.my.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.my.utils.BulletUtils;
import com.my.utils.base.Base3DGame;
import com.my.utils.bool.BooleanOperationException;
import com.my.utils.world.World;
import com.my.utils.world.mod.ModelComponent;
import com.my.utils.world.mod.ModelModule;
import com.my.utils.world.mod.PhyComponent;
import com.my.utils.world.mod.PhyModule;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MyGame extends Base3DGame {

    private Environment environment;
    private World world;
    private ModelModule modelModule;
    private PhyModule phyModule;
    private ArrayMap<String, Model> models = new ArrayMap<>();
    private Matrix4 tmp = new Matrix4();
    private MyInstance cursor;

    public void create() {
        super.create();
        // Init Bullet
        Bullet.init();
        // Create environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.2f, -0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.2f, 0.8f, -1f));
        // Loading Assets
        assetManager.load("obj/sky.g3db", Model.class);
        assetManager.load("obj/bullet.g3db", Model.class);
        assetManager.load("ice/body.g3db", Model.class);
        assetManager.load("ice/cutter.g3db", Model.class);
        assetManager.load("girl/girl.g3db", Model.class);
        waitLoad(true);
        // Init World
        initWorld();
    }
    protected void initUI() {
        // Add Button
        ui.addWidget("btn", new TextButton("Add", ui.skin));
        ui.getWidget("btn", Button.class).addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                addBody();
            }
        });
        ui.addWidget("btn2", new TextButton("Cut", ui.skin));
        ui.getWidget("btn2", Button.class).addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cut();
            }
        });
        // Add Label
        ui.addWidget("label", new Label("", ui.skin));
        ui.getWidget("label", Label.class).setWrap(true);

        inputMultiplexer.addProcessor(new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.F) addBody();
                if (keycode == Input.Keys.C) cut();
                return false;
            }
        });
    }
    public void initWorld() {
        // Init world
        world = new World();
        // Create modelModule
        modelModule = new ModelModule();
        world.addModule("model", modelModule);
        addDisposable(modelModule);
        // Create phyModule
        phyModule = new PhyModule();
        world.addModule("phy", phyModule);
        addDisposable(phyModule);
    }

    protected void doneLoading() {
        initModel();
        initConfig();
        initObject();
    }
    private void initModel() {
        // Build Models
        long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        ModelBuilder mdBuilder = new ModelBuilder();
        models.put("sky", assetManager.get("obj/sky.g3db", Model.class));
        models.put("bullet", assetManager.get("obj/bullet.g3db", Model.class));
        models.put("ground", mdBuilder.createBox(200f, 0.01f, 200f, new Material(ColorAttribute.createDiffuse(Color.WHITE)), attributes));
        models.put("body", assetManager.get("ice/body.g3db", Model.class));
        models.put("girl", assetManager.get("girl/girl.g3db", Model.class));
        models.get("body").nodes.removeValue(models.get("body").getNode("head"), false);
        models.get("body").nodes.removeValue(models.get("body").getNode("hair"), false);
        for(int i=0; i<models.get("body").nodes.size; i++)
            models.get("body").nodes.get(i).translation.set(0, -0.82f, 0);

        Material material = new Material(ColorAttribute.createDiffuse(0, 0, 0, 0.5f));
        models.put("box", mdBuilder.createBox(2, 2, 2, material, attributes));
        models.put("cursor1", mdBuilder.createBox(0.1f, 0.1f, 2, material, attributes));
        models.put("cursor2", mdBuilder.createBox(0.2f, 0.2f, 2, material, attributes));
        models.put("cursor3", mdBuilder.createBox(1, 1, 1, material, attributes));
        models.put("cursor4", mdBuilder.createBox(10, 10, 10, material, attributes));
        models.put("cursor5", mdBuilder.createBox(0.05f, 10, 10, material, attributes));

        models.get("cursor1").nodes.first().parts.first().meshPart.primitiveType = GL20.GL_LINES;
        models.get("cursor2").nodes.first().parts.first().meshPart.primitiveType = GL20.GL_LINES;
        models.get("cursor3").nodes.first().parts.first().meshPart.primitiveType = GL20.GL_LINES;
        models.get("cursor4").nodes.first().parts.first().meshPart.primitiveType = GL20.GL_LINES;
        models.get("cursor5").nodes.first().parts.first().meshPart.primitiveType = GL20.GL_LINES;
    }
    private void initConfig() {
        // Add Model Config
        ModelComponent.addConfig("sky", new ModelComponent.Config(models.get("sky"), false));
        ModelComponent.addConfig("bullet", new ModelComponent.Config(models.get("bullet")));
        ModelComponent.addConfig("box", new ModelComponent.Config(models.get("box")));
        ModelComponent.addConfig("ground", new ModelComponent.Config(models.get("ground")));
        ModelComponent.addConfig("body", new ModelComponent.Config(models.get("body")));
        ModelComponent.addConfig("girl", new ModelComponent.Config(models.get("girl")));
        ModelComponent.addConfig("cursor", new ModelComponent.Config(models.get("cursor4"), false));

        // Add Phy Config
        PhyComponent.addConfig("bullet", new PhyComponent.Config(new btBoxShape(new Vector3(0.025f, 0.025f, 2f)), 1f));
        PhyComponent.addConfig("box", new PhyComponent.Config(BulletUtils.getConvexHullShape(models.get("box")), 50f));
        PhyComponent.addConfig("ground", new PhyComponent.Config(new btBoxShape(new Vector3(100,0.005f,100)), 0f));
//        PhyComponent.addConfig("body", new PhyComponent.Config(new btBoxShape(new Vector3(0.5f, 0.65f, 0.1f)), 50f));
        PhyComponent.addConfig("body", new PhyComponent.Config(BulletUtils.getConvexHullShape(models.get("body")), 0));
        PhyComponent.addConfig("girl", new PhyComponent.Config(BulletUtils.getConvexHullShape(models.get("girl")), 50f));
    }
    private void initObject() {
        addObject("sky", "sky", null);
        addObject("ground", "ground", null);
        addObject("cursor", "cursorTest", null);
        addObject("body", "boolTest", tmp.translate(0, 2, -5));

        cursor = new MyInstance("cursor");
        world.addInstance("cursor", cursor);
    }

    protected void myRender() {
        // Update Camera
        cameraControllerMultiplexer.update();
        ui.getWidget("label", Label.class).setText("Cam: " + cameraControllerMultiplexer.getActivatedCameraName());
        // Update Modules
        phyModule.update();
        // Update Cursor
        cursor.setTransform(tmp.set(cam.view).inv().translate(5, 0, -5.1f));
        // Render
        modelModule.render(cam, environment);
//        phyModule.renderDebug(cam);
        // Remove Old Object
        removeOldBody();
    }

    private void cut() {
        String name = phyModule.pick(cam, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        System.out.println(name);
        if (name == null || !name.startsWith("boolTest")) return;
        BooleanInstance instance = (BooleanInstance) world.getInstance(name);
        try {
            Array<BooleanInstance> newInstances = instance.cut(cursor.modelComponent.getModelInstance().model,
                    cursor.modelComponent.getTransform());
            world.removeInstance(name);
            for (BooleanInstance newInstance : newInstances) {
                world.addInstance("boolTest" + num++, newInstance);
            }
        } catch (BooleanOperationException e) {
            world.removeInstance(name);
            e.printStackTrace();
        }
    }

    private int num = 0;
    private Map<String, Float> bodies = new HashMap<>();
    private void addObject(String config, String name, Matrix4 transform) {
        System.out.println("Add: " + name + "(" + config + ")");
        BooleanInstance instance  = new BooleanInstance(config);
        if (transform != null) instance.setTransform(transform);
        world.addInstance(name, instance);
    }
    private void addBody() {
        String name = "body-" + num++;
        addObject("body", name, tmp.set(cam.view).inv().translate(0, 0, -5));
        bodies.put(name, 5f);
    }
    private void removeOldBody() {
        Iterator<Map.Entry<String, Float>> it = bodies.entrySet().iterator();
        boolean isRemoved = false;
        while (it.hasNext()) {
            Map.Entry<String, Float> entry = it.next();
            if (entry.getValue() <= 0) {
                System.out.println("Remove: " + entry.getKey());
                world.removeInstance(entry.getKey()).dispose();
                it.remove();
                isRemoved = true;
            } else {
                entry.setValue(entry.getValue()- Gdx.graphics.getDeltaTime());
            }
        }
        if (isRemoved) {
            System.out.println("ModelComponent Pool: " + ModelComponent.pool.getFree());
            System.out.println("PhyComponent Pool: " + PhyComponent.pool.getFree());
        }
    }
}
