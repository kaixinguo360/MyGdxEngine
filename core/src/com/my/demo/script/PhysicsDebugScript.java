package com.my.demo.script;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.physics.PhysicsSystem;
import com.my.world.module.render.RenderSystem;
import com.my.world.module.script.ScriptSystem;

public class PhysicsDebugScript implements ScriptSystem.OnStart, RenderSystem.AfterRender {

    private PhysicsSystem physicsSystem;

    @Override
    public void start(Scene scene, Entity entity) {
        physicsSystem = scene.getSystemManager().getSystem(PhysicsSystem.class);
    }

    @Override
    public void afterRender(PerspectiveCamera cam, Environment environment) {
        physicsSystem.renderDebug(cam);
    }
}
