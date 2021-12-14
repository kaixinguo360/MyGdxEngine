package com.my.utils.world.com;

import com.my.utils.world.*;
import com.my.utils.world.sys.PhysicsSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class Collision implements Component, LoadableResource {

    public int callbackFlag;
    public int callbackFilter;
    public PhysicsSystem.CollisionHandler handler;

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        this.callbackFlag = (int) config.get("callbackFilter");
        this.callbackFlag = (int) config.get("callbackFlag");
        this.handler = assetsManager.getAsset((String) config.get("handlerName"), PhysicsSystem.CollisionHandler.class);
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        return new HashMap<String, Object>() {{
            put("callbackFilter", callbackFilter);
            put("callbackFlag", callbackFlag);
            put("handlerName", assetsManager.getId(PhysicsSystem.CollisionHandler.class, handler));
        }};
    }
}
