package com.my.world.enhanced.script;

import com.badlogic.gdx.graphics.Camera;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.camera.CameraSystem;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.script.ScriptSystem;

public class PhysicsDebugScript implements ScriptSystem.OnStart, CameraSystem.AfterRender {

    private PhysicsSystem physicsSystem;

    @Override
    public void start(Scene scene, Entity entity) {
        physicsSystem = scene.getSystemManager().getSystem(PhysicsSystem.class);
    }

    @Override
    public void afterRender(Camera cam) {
        physicsSystem.renderDebug(cam);
    }
}
