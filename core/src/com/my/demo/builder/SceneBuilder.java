package com.my.demo.builder;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.ExitScript;
import com.my.demo.script.GUIScript;
import com.my.demo.script.ReloadScript;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.render.CameraSystem;
import com.my.world.module.render.ModelRender;
import com.my.world.module.render.PresetModelRender;
import com.my.world.module.render.attribute.ColorAttribute;
import com.my.world.module.render.light.DirectionalLight;

import java.util.HashMap;

public class SceneBuilder {

    public static final long attributes = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    public static void initScene(Scene scene) {

        // ----- Init Environments ----- //

        Entity lightEntity = new Entity();
        lightEntity.addComponent(new ColorAttribute(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.AmbientLight, new Color(0.4f, 0.4f, 0.4f, 1f)));
        lightEntity.addComponent(new ColorAttribute(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.Fog, new Color(0.8f, 0.8f, 0.8f, 1f)));
        lightEntity.addComponent(new DirectionalLight(new Color(0.8f, 0.8f, 0.8f, 1f), new Vector3(-0.2f, -0.8f, 1f)));
        lightEntity.addComponent(new DirectionalLight(new Color(0.8f, 0.8f, 0.8f, 1f), new Vector3(0.2f, 0.8f, -1f)));
        scene.getEntityManager().addEntity(lightEntity);

        // ----- Init Static Objects ----- //

        Entity sky = new Entity();
        sky.setName("sky");
        sky.addComponent(new Position(new Matrix4()));
        sky.addComponent(new PresetModelRender(scene.getAsset("sky", ModelRender.class))).includeEnv = false;
        scene.addEntity(sky);
        scene.getSystemManager().getSystem(CameraSystem.class).addSkyBox(sky.getId());

        Entity ground = new Entity();
        ground.setName("ground");
        ground.addComponent(new Position(new Matrix4()));
        ground.addComponent(new PresetModelRender(scene.getAsset("ground", ModelRender.class)));
        ground.addComponent(new PresetTemplateRigidBody(scene.getAsset("ground", TemplateRigidBody.class)));
        scene.addEntity(ground);

        // ----- Init Dynamic Objects ----- //

        scene.instantiatePrefab("Runway");

        for (int i = 1; i < 5; i++) {
            for (int j = 0; j < i; j++) {
                Entity entity = scene.instantiatePrefab("Tower");
                entity.getComponent(Position.class).getLocalTransform().setToTranslation(-5, 5 * j, -200 * i);
            }
        }

        int aircraftNum = 0;
        for (int x = -20; x <= 20; x+=40) {
            for (int y = 0; y <= 0; y+=20) {
                for (int z = -20; z <= 20; z+=20) {
                    Entity entity = scene.instantiatePrefab("Aircraft");
                    entity.setName("Aircraft-" + aircraftNum++);
                    entity.getComponent(Position.class).getLocalTransform().setToTranslation(x, y, z);
                }
            }
        }

        Entity aircraftEntity = scene.instantiatePrefab("Aircraft");
        aircraftEntity.setName("Aircraft-6");
        aircraftEntity.getComponent(Position.class).getLocalTransform().translate(0, 0, 200);
//        aircraftEntity.findChildByName("body").addComponent(new Camera(0, 0, 1, 1, 0));
        scene.instantiatePrefab("Camera", new HashMap<String, Object>() {{
            put("Camera.components[3].config.translateTarget", new Vector3(0, 0.8f, -1.5f));
            put("Camera.parent", aircraftEntity.findChildByName("body"));
            put("Camera.name", "camera");
        }});

        Entity gunEntity = scene.instantiatePrefab("Gun");
        gunEntity.setName("Gun-0");
        gunEntity.getComponent(Position.class).getLocalTransform().translate(0, 0.01f / 2, -20);
        Entity rotateY = gunEntity.findChildByName("rotate_Y");
        Matrix4 rotateYTransform = new Matrix4().translate(0, 0, -20).translate(0, 0.5f + 0.01f / 2, 0);
        Matrix4 groundTransform = ground.getComponent(Position.class).getGlobalTransform().cpy();
        rotateY.addComponent(new HingeConstraint(ground,
                groundTransform.inv().mul(rotateYTransform).rotate(Vector3.X, 90),
                new Matrix4().rotate(Vector3.X, 90),
                false
        ));
//        gunEntity.findChildByName("barrel").addComponent(new Camera(0, 0.7f, 0.3f, 1, 1));
        scene.instantiatePrefab("Camera", new HashMap<String, Object>() {{
            put("Camera.components[2].config.active", false);
            put("Camera.components[3].config.translateTarget", new Vector3(0, 0.8f, -1.5f));
            put("Camera.parent", gunEntity.findChildByName("barrel"));
            put("Camera.name", "camera");
        }});

        // ----- Init Scripts ----- //

        Entity exitScriptEntity = new Entity();
        exitScriptEntity.setName("exitScriptEntity");
        exitScriptEntity.addComponent(new ExitScript());
        scene.addEntity(exitScriptEntity);

        Entity reloadScriptEntity = new Entity();
        reloadScriptEntity.setName("reloadScriptEntity");
        reloadScriptEntity.addComponent(new ReloadScript());
        scene.addEntity(reloadScriptEntity);

        Entity guiEntity = new Entity();
        guiEntity.setName("guiEntity");
        guiEntity.addComponent(new GUIScript()).targetEntity = scene.getEntityManager().findEntityByName("Aircraft-6");
        scene.addEntity(guiEntity);
    }

    public static void init(Engine engine) {
        Scene scene = engine.getSceneManager().newScene("prefab");

        ObjectBuilder.initAssets(engine, scene);
        BulletBuilder.initAssets(engine, scene);
        GunBuilder.initAssets(engine, scene);
        AircraftBuilder.initAssets(engine, scene);

        engine.getSceneManager().removeScene(scene.getId());
    }
}
