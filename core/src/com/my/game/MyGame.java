package com.my.game;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.my.utils.base.Base3DGame;

public class MyGame extends Base3DGame {

    private Environment environment;
    @Override
    public void create() {
        super.create();
        // Create environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.2f, -0.8f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.2f, 0.8f, -1f));
        // Loading Assets
        assetManager.load("obj/sky.g3db", Model.class);
        waitLoad(true);
    }

    @Override
    protected void initUI() {
        // Add List
        List<String> list = new List<>(ui.skin);
        list.setItems("123", "234", "345", "test");
        list.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Selected: " + ((List) actor).getSelected());
            }
        });
        ui.addWidget("list", list);

        // Add Button1, Button2
        ui.addWidget("button1", new TextButton("Click me!", ui.skin));
        ui.addWidget("button2", new TextButton("Click me2!", ui.skin));

        // Add Button3
        TextButton button = new TextButton("Click me3!", ui.skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Button3 Clicked");
            }
        });
        ui.addWidget("button3", button);

        // Add Label
        Label label = new Label("", ui.skin);
        label.setWrap(true);
//        label.getStyle().fontColor.set(1, 1, 1, 1);
        ui.addWidget("label", label);
    }

    private ModelInstance sky;
    @Override
    protected void doneLoading() {
        System.out.println("doneLoading");
        sky = new ModelInstance(assetManager.get("obj/sky.g3db", Model.class));
        sky.transform.translate(2, 2, 2);
    }

    @Override
    protected void myRender() {
        // Update Camera
        cameraControllerMultiplexer.update();
        ui.getWidget("label", Label.class).setText("Cam: " + cameraControllerMultiplexer.getActivatedCameraName());
        // Render
        batch.begin(cam);
        batch.render(sky);
        batch.end();
    }
}
