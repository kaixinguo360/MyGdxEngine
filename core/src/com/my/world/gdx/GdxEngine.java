package com.my.world.gdx;

import com.my.world.core.Engine;
import com.my.world.core.LoadableLoader;
import com.my.world.core.Loader;
import com.my.world.core.SceneLoader;

import java.util.List;

public class GdxEngine extends Engine {

    public GdxEngine() {
        List<Loader> loaders = getLoaderManager().getLoaders();
        loaders.add(new SceneLoader());
        loaders.add(new Matrix4Loader());
        loaders.add(new Vector3Loader());
        loaders.add(new QuaternionLoader());
        loaders.add(new ColorLoader());
        loaders.add(new LoadableLoader());
    }
}
