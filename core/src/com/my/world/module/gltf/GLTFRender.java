package com.my.world.module.gltf;

import com.my.world.core.Component;
import com.my.world.core.Configurable;
import com.my.world.module.common.ActivatableComponent;
import net.mgsx.gltf.scene3d.scene.Scene;

public abstract class GLTFRender extends ActivatableComponent implements Component, Configurable.OnInit {

    public Scene scene;

}
