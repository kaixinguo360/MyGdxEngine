package com.my.world.module.gltf.render;

import com.my.world.core.Component;
import com.my.world.core.Config;
import com.my.world.core.Configurable;
import com.my.world.module.gltf.GLTFRender;
import lombok.NoArgsConstructor;
import net.mgsx.gltf.scene3d.scene.Scene;

@NoArgsConstructor
public class GLTFModelInstance extends GLTFRender implements Component, Configurable.OnInit {

    @Config(type = Config.Type.Asset)
    public GLTFModel model;

    public GLTFModelInstance(GLTFModel model) {
        this.model = model;
        init();
    }

    @Override
    public void init() {
        scene = new Scene(model.sceneAsset.scene);
    }
}
