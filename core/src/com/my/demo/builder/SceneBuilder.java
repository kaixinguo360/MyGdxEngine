package com.my.demo.builder;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.script.*;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.gltf.light.GLTFDirectionalLight;
import com.my.world.module.gltf.light.GLTFSpotLight;
import com.my.world.module.gltf.render.GLTFModel;
import com.my.world.module.gltf.render.GLTFModelInstance;
import com.my.world.module.physics.PresetTemplateRigidBody;
import com.my.world.module.physics.TemplateRigidBody;
import com.my.world.module.physics.constraint.HingeConstraint;
import com.my.world.module.physics.script.EnhancedCharacterController;
import com.my.world.module.render.attribute.ColorAttribute;

import java.util.HashMap;

public class SceneBuilder {

    public static void initScene(Scene scene) {

        // ----- Init Environments ----- //

        Entity lightEntity = new Entity();
        lightEntity.addComponent(new ColorAttribute(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.AmbientLight, new Color(0.4f, 0.4f, 0.4f, 1f)));
        lightEntity.addComponent(new ColorAttribute(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.Fog, new Color(0.8f, 0.8f, 0.8f, 1f)));
        lightEntity.addComponent(new GLTFDirectionalLight(new Color(0.8f, 0.8f, 0.8f, 1f), 1f, new Vector3(-0.2f, -0.8f, 1f)));
        lightEntity.addComponent(new GLTFDirectionalLight(new Color(0.8f, 0.8f, 0.8f, 1f), 1f, new Vector3(0.2f, 0.8f, -1f)));
        scene.getEntityManager().addEntity(lightEntity);

        // ----- Init Static Objects ----- //

        Entity ground = new Entity();
        ground.setName("ground");
        ground.addComponent(new Position(new Matrix4()));
        ground.addComponent(new GLTFModelInstance(scene.getAsset("ground", GLTFModel.class)));
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
                    int finalAircraftNum = aircraftNum;
                    Matrix4 transform = new Matrix4().setToTranslation(x, y, z);
                    scene.instantiatePrefab("Aircraft", new HashMap<String, Object>() {{
                        put("Aircraft.config.components[0].config.localTransform", transform);
                        put("Aircraft.config.components[1]", null);
                        put("Aircraft.config.name", "Aircraft-" + finalAircraftNum);
                    }});
                    aircraftNum++;
                }
            }
        }

        Entity aircraftEntity = scene.instantiatePrefab("Aircraft", new HashMap<String, Object>() {{
            put("Aircraft.config.components[0].config.localTransform", new Matrix4().setToTranslation(0, 0, 200));
            put("Aircraft.config.name", "Aircraft-6");
        }});
        aircraftEntity.addComponent(new CharacterSwitcherAgent()).characterName = "aircraft";
        scene.instantiatePrefab("Camera", new HashMap<String, Object>() {{
            put("Camera.config.components[3].config.translateTarget", new Vector3(0, 0.8f, -1.5f));
            put("Camera.config.parent", aircraftEntity.findChildByName("body"));
            put("Camera.config.name", "camera");
        }});

        Entity gunEntity = scene.instantiatePrefab("Gun", new HashMap<String, Object>() {{
            put("Gun.config.components[0].config.localTransform", new Matrix4().setToTranslation(0, 0.01f / 2, -20));
            put("Gun.config.components[1].config.active", false);
            put("Gun.config.name", "Gun-0");
        }});
        gunEntity.addComponent(new CharacterSwitcherAgent()).characterName = "gun";
        Entity rotateY = gunEntity.findChildByName("rotate_Y");
        Matrix4 rotateYTransform = new Matrix4().translate(0, 0, -20).translate(0, 0.5f + 0.01f / 2, 0);
        Matrix4 groundTransform = ground.getComponent(Position.class).getGlobalTransform(new Matrix4());
        rotateY.addComponent(new HingeConstraint(ground,
                groundTransform.inv().mul(rotateYTransform).rotate(Vector3.X, 90),
                new Matrix4().rotate(Vector3.X, 90),
                false
        ));
        scene.instantiatePrefab("Camera", new HashMap<String, Object>() {{
            put("Camera.config.components[2].config.active", false);
            put("Camera.config.components[3].config.translateTarget", new Vector3(0, 0.8f, -1.5f));
            put("Camera.config.parent", gunEntity.findChildByName("barrel"));
            put("Camera.config.name", "camera");
        }});

        Entity character = new Entity();
        character.addComponent(new Position(new Matrix4().translate(0, 1, 0).rotate(Vector3.Y, 180)));
        character.addComponent(new GLTFModelInstance(scene.getAsset("bullet", GLTFModel.class)));
        EnhancedCharacterController characterController = character.addComponent(new EnhancedCharacterController());
        characterController.setActive(false);
        characterController.shape = scene.getAsset("bullet", TemplateRigidBody.class);
        characterController.velocity = 30;
        characterController.mass = 0.5f;
        characterController.keyUp = Input.Keys.W;
        characterController.keyDown = Input.Keys.S;
        characterController.keyLeft = Input.Keys.A;
        characterController.keyRight = Input.Keys.D;
        characterController.keyJump = Input.Keys.SPACE;
        character.addComponent(new CharacterSwitcherAgent()).characterName = "character";
        scene.addEntity(character);
        scene.instantiatePrefab("Camera", new HashMap<String, Object>() {{
            put("Camera.config.components[2].config.active", false);
            put("Camera.config.components[3].config.yawLocked", true);
            put("Camera.config.components[3].config.yaw", -180);
            put("Camera.config.components[3].config.yawTarget", -180);
            put("Camera.config.components[3].config.recoverEnabled", false);
            put("Camera.config.parent", character);
            put("Camera.config.name", "camera");
        }});

        Entity spotLight = new Entity();
        spotLight.setName("spotLight");
        spotLight.addComponent(new Position(new Matrix4().translate(0, 1, 0)));
        spotLight.addComponent(new GLTFSpotLight(Color.WHITE.cpy(), Vector3.Z.cpy(), 1f, 10f));
        spotLight.setParent(character);
        scene.addEntity(spotLight);

        // ----- Init Scripts ----- //

        Entity exitScriptEntity = new Entity();
        exitScriptEntity.setName("exitScriptEntity");
        exitScriptEntity.addComponent(new ExitScript());
        scene.addEntity(exitScriptEntity);

        Entity reloadScriptEntity = new Entity();
        reloadScriptEntity.setName("reloadScriptEntity");
        reloadScriptEntity.addComponent(new ReloadScript());
        scene.addEntity(reloadScriptEntity);

//        Entity physicsDebugScriptEntity = new Entity();
//        physicsDebugScriptEntity.setName("physicsDebugScriptEntity");
//        physicsDebugScriptEntity.addComponent(new PhysicsDebugScript());
//        scene.addEntity(physicsDebugScriptEntity);

        Entity guiEntity = new Entity();
        guiEntity.setName("guiEntity");
        guiEntity.addComponent(new GUIScript()).targetEntity = scene.getEntityManager().findEntityByName("Aircraft-6");
        scene.addEntity(guiEntity);

        Entity characterSelectorEntity = new Entity();
        characterSelectorEntity.setName("characterSelectorEntity");
        CharacterSwitcher characterSwitcher = characterSelectorEntity.addComponent(new CharacterSwitcher());
        characterSwitcher.characterNames.add("aircraft");
        characterSwitcher.characterNames.add("gun");
        characterSwitcher.characterNames.add("character");
        scene.addEntity(characterSelectorEntity);
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
