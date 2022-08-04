package com.my.demo.builder.object;

import com.badlogic.gdx.math.Matrix4;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.enhanced.builder.PrefabBuilder;
import com.my.world.module.camera.Camera;
import com.my.world.module.camera.script.EnhancedThirdPersonCameraController;
import com.my.world.module.common.Position;
import com.my.world.module.render.model.GLTFModel;

public class CameraBuilder extends PrefabBuilder<CameraBuilder> {

    {
        prefabName = "Camera";
    }

    @Override
    public void createPrefab(Scene scene) {
        Entity entity = new Entity();
        entity.setName("Camera");
        entity.addComponent(new Position(new Matrix4()));
        entity.addComponent(new GLTFModel("obj/camera.gltf")).setActive(false);
        entity.addComponent(new Camera(0, 0, 1, 1, 0));
        EnhancedThirdPersonCameraController cameraController = entity.addComponent(new EnhancedThirdPersonCameraController());
        cameraController.centerTarget.set(0, 0, 0);
        cameraController.translateTarget.set(0, 0, 20);
        cameraController.translate.set(0, 0, 20);
        cameraController.recoverRate = 2f;
        cameraController.waitTime = 10;
        scene.addEntity(entity);
    }
}
