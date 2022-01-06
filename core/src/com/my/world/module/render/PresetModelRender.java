package com.my.world.module.render;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.my.world.core.Config;
import com.my.world.core.Loadable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PresetModelRender extends Render implements Loadable.OnInit {

    @Config(type = Config.Type.Asset)
    public ModelRender modelRender;

    public PresetModelRender(ModelRender modelRender) {
        this.modelRender = modelRender;
        init();
    }

    @Override
    public void init() {
        this.modelInstance = new ModelInstance(modelRender.model);
        this.center.set(modelRender.center);
        this.dimensions.set(modelRender.dimensions);
        this.radius = modelRender.radius;
    }
}
