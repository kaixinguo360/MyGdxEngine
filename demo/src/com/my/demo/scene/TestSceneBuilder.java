package com.my.demo.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.common.AnimationEntity;
import com.my.demo.entity.common.GdxAnimationUtil;
import com.my.demo.entity.object.CharacterEntity;
import com.my.demo.entity.object.GroundEntity;
import com.my.world.core.Component;
import com.my.world.core.Engine;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.BaseBuilder;
import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.enhanced.portal.EnhancedHoleSpaceEntity;
import com.my.world.enhanced.portal.PortalEntity;
import com.my.world.enhanced.ragdoll.ModelEntity;
import com.my.world.module.animation.Animation;
import com.my.world.module.animation.DefaultAnimationController;
import com.my.world.module.camera.Camera;
import com.my.world.module.camera.script.EnhancedThirdPersonCameraController;
import com.my.world.module.common.EnhancedPosition;
import com.my.world.module.common.Position;
import com.my.world.module.input.InputSystem;
import com.my.world.module.script.ScriptSystem;
import net.mgsx.gltf.loaders.glb.GLBAssetLoader;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

import java.util.Map;

import static com.my.demo.entity.common.GdxAnimationUtil.getAnimation;
import static com.my.demo.entity.common.GdxAnimationUtil.hasAnimation;

public class TestSceneBuilder extends BaseBuilder<TestSceneBuilder> {

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
        GroundEntity ground = new GroundEntity();
        ground.addToScene(scene);

//        CharacterEntity character = new CharacterEntity(new CharacterEntity.Param());
//        character.addToScene(scene);
//
//        AnimationEntity animationEntity = new AnimationEntity();
//        animationEntity.addToScene(scene);
//
//        scene.addEntity(newEntity((InputSystem.OnKeyDown) keycode -> {
//            if (keycode == Input.Keys.R) {
//                Animation animation = animationEntity.getComponent(Animation.class);
//                ((DefaultAnimationController) animation.animationController).initState = "state2";
//            }
//        }));

        EnhancedEntity entity = new ModelEntity(ModelEntity.Param.builder()
                .defaultObjectMass(1000f)
                .model(model)
                .rootNodeId("Root")
                .build());
        entity.position.setLocalTransform(new Matrix4().translate(0, 0, 0));
        if (hasAnimation(model)) {
            Animation animation = entity.addComponent(getAnimation(model.animations));
            animation.setActive(false);
            GdxAnimationUtil.GdxAnimationController controller = (GdxAnimationUtil.GdxAnimationController) animation.animationController;
            controller.addTransition(new DefaultAnimationController.Transition() {{
                this.start = 0;
                this.end = 0.5f;
                this.nextState = "default";
                this.canSwitch = instance -> Gdx.input.isKeyPressed(Input.Keys.NUM_1);
            }});
            controller.addTransition(new DefaultAnimationController.Transition() {{
                this.start = 0;
                this.end = 0.5f;
                this.nextState = "up";
                this.canSwitch = instance -> Gdx.input.isKeyPressed(Input.Keys.NUM_2);
            }});
            controller.addTransition(new DefaultAnimationController.Transition() {{
                this.start = 0;
                this.end = 0.5f;
                this.nextState = "low";
                this.canSwitch = instance -> Gdx.input.isKeyPressed(Input.Keys.NUM_4);
            }});
            entity.addComponent((InputSystem.OnKeyDown) keycode -> {
                if (keycode == Input.Keys.NUM_3) {
                    ((GdxAnimationUtil.GdxAnimationController.Instance) animation.controllerInstance).applyAction("test");
                }
                if (keycode == Input.Keys.X) {
                    animation.setActive(false);
                }
                if (keycode == Input.Keys.C) {
                    animation.setActive(true);
                }
            });
        }
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
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                control2.translation.z += v;
                control2.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                control2.translation.z -= v;
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

        Entity leftLeg = entity.findChildByName("Leg_L");
        EnhancedPosition leftLegPosition = leftLeg.getComponent(EnhancedPosition.class);
        leftLegPosition.getLocalTransform().rotate(Vector3.X, -90);

        EnhancedHoleSpaceEntity holeSpaceEntity = new EnhancedHoleSpaceEntity(1f, 4f);
        holeSpaceEntity.position.getLocalTransform().setToTranslation(0, 5, 0).rotate(Vector3.X, 90);
        holeSpaceEntity.addToScene(scene);
        holeSpaceEntity.addRendersFromEntity(leftLeg, false);

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

        PortalEntity portal1 = new PortalEntity(2.5f);
        portal1.setName("portal1");
        portal1.position.getLocalTransform().setToTranslation(5, 2.5f, 5);
        portal1.portalScript.targetPortalName = "portal2";
        portal1.addToScene(scene);

        PortalEntity portal2 = new PortalEntity(2.5f);
        portal2.setName("portal2");
        portal2.position.getLocalTransform().setToTranslation(-5, 2.5f, 5);
        portal2.portalScript.targetPortalName = "portal1";
        portal2.addToScene(scene);

//        scene.getTimeManager().setTimeScale(0);

        return ground;
    }

    public static Entity newEntity(Component component) {
        Entity entity = new Entity();
        entity.addComponent(component);
        return entity;
    }
}
