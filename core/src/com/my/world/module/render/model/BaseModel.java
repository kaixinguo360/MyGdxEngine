package com.my.world.module.render.model;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.my.world.core.Loadable;
import com.my.world.module.render.RenderModel;

public abstract class BaseModel extends RenderModel implements Loadable.OnInit {

    protected static final AssetManager assetManager = new AssetManager();
    protected static final ModelBuilder mdBuilder = new ModelBuilder();

}
