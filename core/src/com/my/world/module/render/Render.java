package com.my.world.module.render;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.my.world.core.Component;
import com.my.world.module.common.Position;

public interface Render extends Component, Component.Activatable, RenderableProvider {

    default boolean isIncludeEnv() {
        return true;
    }

    default Shader getShader() {
        return null;
    }

    default void setTransform(Position position) {
    }

    default boolean isVisible(Camera cam) {
        return true;
    }
}
