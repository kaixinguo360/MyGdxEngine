package com.my.world.module.camera.script;

import com.badlogic.gdx.graphics.Camera;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.common.Position;
import com.my.world.module.script.ScriptSystem;

public class SkyBoxScript implements ScriptSystem.OnStart, CameraSystem.BeforeRender {

    private Position position;

    @Override
    public void start(Scene scene, Entity entity) {
        position = entity.getComponent(Position.class);
    }

    @Override
    public void beforeRender(Camera cam) {
        position.getLocalTransform().setToTranslation(cam.position);
    }
}
