package com.my.world.module.render.model;

import com.badlogic.gdx.graphics.g3d.Model;
import com.my.world.core.Config;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ExternalModel extends BaseModel {

    @Config
    public String path;

    public ExternalModel(String path) {
        this.path = path;
        init();
    }

    @Override
    public void init() {
        assetManager.load(path, Model.class);
        assetManager.finishLoading(); // TODO: Async
        model = assetManager.get(path, Model.class);
        calculateBoundingBox();
    }
}
