package com.my.world.module.render;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.my.world.core.Config;
import com.my.world.module.common.ActivatableComponent;
import com.my.world.module.common.Position;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Render extends ActivatableComponent implements RenderableProvider {

    @Config
    public boolean includeEnv = true;

    @Config(type = Config.Type.Asset)
    public Shader shader;

    public abstract void setTransform(Position position);

    public abstract boolean isVisible(PerspectiveCamera cam);
}
