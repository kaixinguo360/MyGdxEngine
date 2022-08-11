package com.my.demo.scene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.my.demo.entity.object.BoxEntity;
import com.my.demo.entity.object.GroundEntity;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.BaseBuilder;
import com.my.world.enhanced.depthmask.entity.EnhancedHoleSpaceEntity;
import com.my.world.enhanced.portal.PortalEntity;
import com.my.world.module.camera.Camera;
import com.my.world.module.camera.script.EnhancedThirdPersonCameraController;
import com.my.world.module.common.EnhancedPosition;
import com.my.world.module.common.Position;
import com.my.world.module.script.ScriptSystem;

import java.util.Map;

public class PortalScene extends BaseBuilder<PortalScene> {

    @Override
    public Entity build(Scene scene, Map<String, Object> params) {
        GroundEntity ground = new GroundEntity();
        ground.addToScene(scene);

        BoxEntity box = new BoxEntity();
        box.rigidBody.isKinematic = true;
        EnhancedPosition position = box.position;
        box.addComponent((ScriptSystem.OnUpdate) (scene1, entity1) -> {
            float v = 0.1f;
            if (Gdx.input.isKeyPressed(Input.Keys.Z)) {
                position.translation.y += v;
                position.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.C)) {
                position.translation.y -= v;
                position.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                position.translation.x += v;
                position.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                position.translation.x -= v;
                position.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                position.translation.z += v;
                position.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                position.translation.z -= v;
                position.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                position.rotation.x += v * 20;
                position.sync();
            }
            if (Gdx.input.isKeyPressed(Input.Keys.E)) {
                position.rotation.x -= v * 20;
                position.sync();
            }
        });
        box.addToScene(scene);

        EnhancedHoleSpaceEntity holeSpaceEntity = new EnhancedHoleSpaceEntity(1f, 4f);
        holeSpaceEntity.position.getLocalTransform().setToTranslation(0, 5, 0).rotate(Vector3.X, 90);
        holeSpaceEntity.addToScene(scene);
        holeSpaceEntity.addRendersFromEntity(box, false);

        Entity camera = new Entity();
        camera.setName("Camera");
        camera.setParent(box);
        camera.addComponent(new Position(new Matrix4()));
        camera.addComponent(new Camera(0, 0, 1, 1, 0));
        EnhancedThirdPersonCameraController cameraController = camera.addComponent(new EnhancedThirdPersonCameraController());
        cameraController.center.set(0, -2f, 0);
        cameraController.translate.set(0, 0, 20);
        cameraController.recoverEnabled = false;
        scene.addEntity(camera);

        PortalEntity portal1 = new PortalEntity(2.5f);
        portal1.setName("portal1");
        portal1.position.getLocalTransform().setToTranslation(5, 0, 0);
        portal1.portal.targetPortalName = "portal2";
        portal1.addToScene(scene);

        PortalEntity portal2 = new PortalEntity(2.5f);
        portal2.setName("portal2");
        portal2.position.getLocalTransform().setToTranslation(-5, 0, 0);
        portal2.portal.targetPortalName = "portal1";
        portal2.addToScene(scene);

//        scene.getTimeManager().setTimeScale(0);

        return ground;
    }

}
