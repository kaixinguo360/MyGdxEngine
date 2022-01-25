package com.my.world.module.render.model;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.my.world.core.Config;
import com.my.world.core.Configurable;
import com.my.world.module.render.ModelRender;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ExternalModel extends ModelRender implements Configurable.OnInit {

    protected static final AssetManager assetManager = new AssetManager();

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
        super.init();
    }
}
