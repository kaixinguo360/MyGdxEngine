package com.my.world.module.render.model;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.my.world.core.Config;
import com.my.world.core.Configurable;
import com.my.world.module.render.ModelRender;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProceduralModelRender extends ModelRender implements Configurable.OnInit {

    protected static final ModelBuilder mdBuilder = new ModelBuilder();

    @Config(type = Config.Type.Asset)
    protected Material material;

    @Config
    protected long attributes;

}
