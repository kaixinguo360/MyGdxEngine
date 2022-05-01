package com.my.demo.builder.scene.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.my.demo.builder.BaseBuilder;
import com.my.demo.builder.enhanced.EnhancedEntity;
import com.my.demo.builder.object.CharacterBuilder;
import com.my.demo.builder.object.GroundBuilder;
import com.my.demo.builder.test.AnimationBuilder;
import com.my.demo.builder.test.ModelEntity;
import com.my.world.core.Component;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.camera.Camera;
import com.my.world.module.camera.script.EnhancedThirdPersonCameraController;
import com.my.world.module.common.EnhancedPosition;
import com.my.world.module.common.Position;
import com.my.world.module.script.ScriptSystem;
import net.mgsx.gltf.loaders.glb.GLBAssetLoader;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestSceneBuilder extends BaseBuilder<TestSceneBuilder> {

    public GroundBuilder groundBuilder;
    public CharacterBuilder characterBuilder;
    public AnimationBuilder animationBuilder;

    @Override
    protected void initDependencies() {
        groundBuilder = getDependency(GroundBuilder.class);
        characterBuilder = getDependency(CharacterBuilder.class);
        animationBuilder = getDependency(AnimationBuilder.class);
    }

    public Model model;

    @Override
    protected void createAssets(Engine engine, Scene scene) {
        String path = "test/test.glb";
        AssetManager assetManager = new AssetManager();
        assetManager.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
        assetManager.setLoader(SceneAsset.class, ".glb", new GLBAssetLoader());
        assetManager.load(path, SceneAsset.class);
        assetManager.finishLoading();
        SceneAsset sceneAsset = assetManager.get(path, SceneAsset.class);
        model = assetsManager.addAsset(path, Model.class, sceneAsset.scene.model);
    }

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        Entity ground = groundBuilder.build(scene, null);

//        characterBuilder.build(scene, CharacterBuilder.Param.builder()
//                .velocity(10)
//                .build());
//
//        Entity animationEntity = animationBuilder.build(scene);
//        scene.addEntity(newEntity((InputSystem.OnKeyDown) keycode -> {
//            if (keycode == Input.Keys.R) {
//                Animation animation = animationEntity.getComponent(Animation.class);
//                animation.animationController.initState = "state2";
//            }
//        }));

        EnhancedEntity entity = new ModelEntity(ModelEntity.Param.builder()
                .defaultObjectMass(1000f)
                .model(model)
                .rootNodeId("Root")
                .build());
        entity.position.setLocalTransform(new Matrix4().translate(0, 0, -5));
        EnhancedPosition control1 = entity.findChildByName("Control_1").getComponent(EnhancedPosition.class);
        EnhancedPosition control2 = entity.findChildByName("Control_2").getComponent(EnhancedPosition.class);
        EnhancedPosition control3 = entity.findChildByName("Control_3").getComponent(EnhancedPosition.class);
        entity.addComponent((ScriptSystem.OnUpdate) (scene1, entity1) -> {
            float v = 0.1f;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                control1.translation.y += v;
                control1.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                control1.translation.y -= v;
                control1.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                control2.translation.x += v;
                control2.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                control2.translation.x -= v;
                control2.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                control3.rotation.x += v * 20;
                control3.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                control3.rotation.x -= v * 20;
                control3.sync();
            }
        });
        entity.addToScene(scene);

        Entity camera = new Entity();
        camera.setName("Camera");
        camera.setParent(entity.findChildByName("Control_2"));
        camera.addComponent(new Position(new Matrix4()));
        camera.addComponent(new Camera(0, 0, 1, 1, 0));
        EnhancedThirdPersonCameraController cameraController = camera.addComponent(new EnhancedThirdPersonCameraController());
        cameraController.center.set(0, -2f, 0);
        cameraController.translate.set(0, 0, 20);
        cameraController.recoverEnabled = false;
        scene.addEntity(camera);

//        scene.getTimeManager().setTimeScale(0);

        return ground;
    }

    public static final Map<String, Pattern> regexes = new HashMap<>();
    public static <T> T regex(String str, String regex, Function<String, T> consumer) {
        Pattern r = regexes.get(regex);
        if (r == null) {
            r = Pattern.compile(regex);
            regexes.put(regex, r);
        }
        Matcher m = r.matcher(str);
        if (m.find()) {
            return consumer.apply(m.group(1));
        }
        return null;
    }

    public static Entity newEntity(Component component) {
        Entity entity = new Entity();
        entity.addComponent(component);
        return entity;
    }
}
