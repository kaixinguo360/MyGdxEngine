package com.my.utils.world.com;

import com.my.utils.world.AssetsManager;
import com.my.utils.world.Loader;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.World;
import com.my.utils.world.sys.ScriptSystem;

import java.util.HashMap;
import java.util.Map;

public class ScriptComponentLoader implements Loader {

    private LoaderManager loaderManager;
    private AssetsManager assetsManager;

    public ScriptComponentLoader(LoaderManager loaderManager) {
        this.loaderManager = loaderManager;
    }

    private AssetsManager getAssetsManager() {
        World world = (World) loaderManager.getEnvironment().get("world");
        if (world == null) throw new RuntimeException("Required params not set: world");
        return world.getAssetsManager();
    }

    @Override
    public <E, T> T load(E config, Class<T> type) {
        if (assetsManager == null) assetsManager = getAssetsManager();
        Map<String, Object> map = (Map<String, Object>) config;
        return (T) new ScriptComponent(
                assetsManager.getAsset((String) map.get("script"), ScriptSystem.Script.class),
                (Map<String, Object>) map.get("config"),
                null
        );
    }

    @Override
    public <E, T> E getConfig(T obj, Class<E> configType) {
        if (assetsManager == null) assetsManager = getAssetsManager();
        ScriptComponent scriptComponent = (ScriptComponent) obj;
        return (E) new HashMap<String, Object>() {{
            put("script", assetsManager.getId(ScriptSystem.Script.class, scriptComponent.script));
            put("config", scriptComponent.config);
        }};
    }

    @Override
    public <E, T> boolean handleable(Class<E> configType, Class<T> targetType) {
        return (Map.class.isAssignableFrom(configType)) && (targetType == ScriptComponent.class);
    }
}
