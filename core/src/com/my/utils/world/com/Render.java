package com.my.utils.world.com;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.Component;
import com.my.utils.world.Config;
import com.my.utils.world.StandaloneResource;
import com.my.utils.world.sys.RenderSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Render implements Component, StandaloneResource, StandaloneResource.OnInit {

    @Config(type = Config.Type.Asset)
    public RenderSystem.RenderModel renderModel;

    @Config
    public boolean includeEnv = true;

    public ModelInstance modelInstance;
    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public float radius;

    public Render(RenderSystem.RenderModel renderModel) {
        this.renderModel = renderModel;
        init();
    }

    @Override
    public void init() {
        this.modelInstance = new ModelInstance(renderModel.model);
        this.center.set(renderModel.center);
        this.dimensions.set(renderModel.dimensions);
        this.radius = renderModel.radius;
    }
}
