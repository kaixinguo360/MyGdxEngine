package com.my.game.model;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.my.utils.world.Loadable;
import com.my.utils.world.com.RenderModel;

public abstract class BaseModel extends RenderModel implements Loadable.OnInit {

    protected static final AssetManager assetManager = new AssetManager();
    protected static final ModelBuilder mdBuilder = new ModelBuilder();

}
