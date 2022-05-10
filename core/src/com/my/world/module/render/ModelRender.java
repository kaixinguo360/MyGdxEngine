package com.my.world.module.render;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.my.world.core.Configurable;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ModelRender extends BaseRender implements Configurable.OnInit {

    public Model model;

    public ModelRender(Model model) {
        this.model = model;
        init();
    }

    @Override
    public void init() {
        this.modelInstance = new ModelInstance(this.model);
        calculateBoundingBox();
    }
}
