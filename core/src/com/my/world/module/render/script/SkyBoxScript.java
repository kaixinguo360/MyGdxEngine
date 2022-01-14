package com.my.world.module.render.script;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.my.world.core.Entity;
import com.my.world.core.Scene;
import com.my.world.module.common.Position;
import com.my.world.module.render.RenderSystem;
import com.my.world.module.script.ScriptSystem;

public class SkyBoxScript implements ScriptSystem.OnStart, RenderSystem.BeforeRender {

    private Position position;

    @Override
    public void start(Scene scene, Entity entity) {
        position = entity.getComponent(Position.class);
    }

    @Override
    public void beforeRender(PerspectiveCamera cam, Environment environment) {
        position.getLocalTransform().setToTranslation(cam.position);
    }
}
