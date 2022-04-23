package com.my.world.module.render.model;

import com.badlogic.gdx.assets.AssetManager;
import com.my.world.core.Component;
import com.my.world.core.Config;
import com.my.world.core.Configurable;
import com.my.world.module.render.BaseRender;
import lombok.NoArgsConstructor;
import net.mgsx.gltf.loaders.glb.GLBAssetLoader;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

@NoArgsConstructor
public class GLTFModel extends BaseRender implements Component, Configurable.OnInit {

    protected static final AssetManager assetManager = new AssetManager();

    static {
        assetManager.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
        assetManager.setLoader(SceneAsset.class, ".glb", new GLBAssetLoader());
    }

    @Config
    public String path;

    public SceneAsset sceneAsset;
    public Scene scene;

    public GLTFModel(String path) {
        this.path = path;
        init();
    }

    @Override
    public void init() {
        assetManager.load(path, SceneAsset.class);
        assetManager.finishLoading();
        sceneAsset = assetManager.get(path, SceneAsset.class);
        scene = new Scene(sceneAsset.scene);
        modelInstance = scene.modelInstance;
        calculateBoundingBox();
    }
}
