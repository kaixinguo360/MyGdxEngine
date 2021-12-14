package com.my.utils.world.com;

import com.my.utils.world.*;
import com.my.utils.world.sys.ScriptSystem;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class ScriptComponent implements Component, LoadableResource {

    public boolean disabled;
    public ScriptSystem.Script script;
    public Map<String, Object> config;
    public Object customObj;

    @Override
    public void load(Map<String, Object> config, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        this.disabled =(Boolean) config.get("disabled");
        this.script = assetsManager.getAsset((String) config.get("script"), ScriptSystem.Script.class);
        this.config = (Map<String, Object>) config.get("config");
        this.customObj = null;
    }

    @Override
    public Map<String, Object> getConfig(Class<Map<String, Object>> configType, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        return new HashMap<String, Object>() {{
            put("disabled", disabled);
            put("script", assetsManager.getId(ScriptSystem.Script.class, script));
            put("config", config);
        }};
    }
}
