package com.my.utils.world.com;

import com.my.utils.world.*;
import com.my.utils.world.sys.MotionSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Motion implements Component, LoadableResource {

    public MotionSystem.MotionHandler handler;
    public Map<String, Object> config;

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        this.handler = assetsManager.getAsset((String) config.get("type"), MotionSystem.MotionHandler.class);
        this.config = (Map<String, Object>) config.get("config");
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        return new HashMap<String, Object>() {{
            put("type", assetsManager.getId(MotionSystem.MotionHandler.class, handler));
            put("config", config);
        }};
    }
}
