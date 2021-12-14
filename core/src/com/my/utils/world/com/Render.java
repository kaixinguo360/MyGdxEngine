package com.my.utils.world.com;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.my.utils.world.*;
import com.my.utils.world.sys.RenderSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Render implements Component, LoadableResource {

    public RenderSystem.RenderConfig renderConfig;
    public ModelInstance modelInstance;
    public boolean includeEnv;
    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public float radius;

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        String renderConfigId = (String) config.get("renderConfigId");
        this.renderConfig  = assetsManager.getAsset(renderConfigId, RenderSystem.RenderConfig.class);
        this.modelInstance = new ModelInstance(renderConfig.model);
        this.includeEnv = renderConfig.includeEnv;
        this.center.set(renderConfig.center);
        this.dimensions.set(renderConfig.dimensions);
        this.radius = renderConfig.radius;
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        String renderConfigId = assetsManager.getId(RenderSystem.RenderConfig.class, renderConfig);
        return new HashMap<String, Object>(){{
            put("renderConfigId", renderConfigId);
        }};
    }
}
