package com.my.demo.entity.object;

import com.my.world.enhanced.entity.EnhancedEntity;
import com.my.world.module.camera.script.EnhancedThirdPersonCameraController;
import com.my.world.module.render.model.GLTFModel;

public class CameraEntity extends EnhancedEntity {

    public final GLTFModel render;
    public final com.my.world.module.camera.Camera camera;
    public final EnhancedThirdPersonCameraController controller;

    public CameraEntity() {
        setName("Camera");
        render = addComponent(new GLTFModel("obj/camera.gltf"));
        render.setActive(false);
        camera = addComponent(new com.my.world.module.camera.Camera(0, 0, 1, 1, 0));
        controller = addComponent(new EnhancedThirdPersonCameraController());
        controller.centerTarget.set(0, 0, 0);
        controller.translateTarget.set(0, 0, 20);
        controller.translate.set(0, 0, 20);
        controller.recoverRate = 2f;
        controller.waitTime = 10;
    }
}
