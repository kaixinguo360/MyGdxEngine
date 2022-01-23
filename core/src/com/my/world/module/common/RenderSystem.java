package com.my.world.module.common;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.my.world.core.System;

public interface RenderSystem extends System {
    void begin();
    void render(PerspectiveCamera cam);
    void end();
}
