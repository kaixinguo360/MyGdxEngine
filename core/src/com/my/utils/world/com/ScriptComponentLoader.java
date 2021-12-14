package com.my.utils.world.com;

import com.my.utils.world.AssetsManager;
import com.my.utils.world.LoadContext;
import com.my.utils.world.Loader;
import com.my.utils.world.World;
import com.my.utils.world.sys.ScriptSystem;

import java.util.HashMap;
import java.util.Map;

public class ScriptComponentLoader implements Loader {

    @Override
    public <E, T> T load(E config, Class<T> type, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        Map<String, Object> map = (Map<String, Object>) config;
        return (T) new ScriptComponent(
                (Boolean) map.get("disabled"),
                assetsManager.getAsset((String) map.get("script"), ScriptSystem.Script.class),
                (Map<String, Object>) map.get("config"),
                null
        );
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType, LoadContext context) {
        AssetsManager assetsManager = context.getEnvironment("world", World.class).getAssetsManager();
        ScriptComponent scriptComponent = (ScriptComponent) obj;
        return (E) new HashMap<String, Object>() {{
            put("disabled", scriptComponent.disabled);
            put("script", assetsManager.getId(ScriptSystem.Script.class, scriptComponent.script));
            put("config", scriptComponent.config);
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == ScriptComponent.class);
    }
}
