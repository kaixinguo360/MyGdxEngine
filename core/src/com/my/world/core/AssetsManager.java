package com.my.world.core;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

public class AssetsManager implements Disposable {

    public static final String CONTEXT_FIELD_NAME = "ASSETS_MANAGER";

    private final Engine engine;

    @Getter
    private final Map<Class<?>, Map<String, Object>> allAssets = new LinkedHashMap<>();

    private final Map<String, String> cache = new HashMap<>();

    AssetsManager(Engine engine) {
        this.engine = engine;
    }

    public <T> T addAsset(String id, Class<?> type, Object asset) {
        if (!type.isInstance(asset)) throw new RuntimeException("Asset type not equal: " + type + " != " + asset.getClass());
        if (!allAssets.containsKey(type)) allAssets.put(type, new LinkedHashMap<>());
        Map<String, Object> assets = allAssets.get(type);
        if (assets.containsKey(id)) throw new RuntimeException("Duplicate Assets: " + id + " (" + type + ")");
        assets.put(id, asset);
        cache.remove(type + "#" + asset.hashCode());
        return (T) asset;
    }
    public <T> T removeAsset(String id, Class<T> type) {
        if (!allAssets.containsKey(type)) throw new RuntimeException("No Such Assets: " + id + " (" + type + ")");
        if (!allAssets.get(type).containsKey(id)) throw new RuntimeException("No Such Assets: " + id + " (" + type + ")");
        T removed = (T) allAssets.get(type).remove(id);
        cache.remove(type + "#" + removed.hashCode());
        return removed;
    }
    public <T> T getAsset(String id, Class<T> type) {
        if (!allAssets.containsKey(type)) throw new RuntimeException("No Such Assets: " + id + " (" + type + ")");
        if (!allAssets.get(type).containsKey(id)) throw new RuntimeException("No Such Assets: " + id + " (" + type + ")");
        return (T) allAssets.get(type).get(id);
    }
    public <T> boolean hasAsset(String id, Class<T> type) {
        return allAssets.containsKey(type) && allAssets.get(type).containsKey(id);
    }
    public <T> String getId(Class<?> type, Object asset) {
        if (asset == null) throw new RuntimeException("Asset is null");
        String hash = type + "#" + asset.hashCode();
        if (cache.containsKey(hash)) {
            return cache.get(hash);
        } else {
            if (!allAssets.containsKey(type)) throw new RuntimeException("No Such Assets: " + hash);
            if (!allAssets.get(type).containsValue(asset)) throw new RuntimeException("No Such Assets: " + hash);
            for (Map.Entry<String, Object> entry : allAssets.get(type).entrySet()) {
                if (entry.getValue() == asset) {
                    cache.put(hash, entry.getKey());
                    return entry.getKey();
                }
            }
            throw new RuntimeException("No Such Assets: " + hash);
        }
    }

    // ----- Load Asset ----- //
    public void loadAssetsFromFile(String path) {
        String yamlConfig = LoaderManager.readFile(path);
        loadAssetsFromYaml(yamlConfig);
    }
    public void loadAssetsFromYaml(String yamlConfig) {
        Yaml yaml = new Yaml();
        for (Object config : yaml.loadAll(yamlConfig)) {
            loadAsset((Map<String, Object>) config);
        }
    }
    public void loadAsset(Map<String, Object> config) {
        try {
            String assetTypeName = (String) config.get("type");
            Class<?> assetType = Class.forName(assetTypeName);
            Object assetConfig = config.get("config");
            String assetId = (String) config.get("id");
            Object asset;
            if (config.containsKey("instanceType")) {
                String instanceTypeName = (String) config.get("instanceType");
                Class<?> instanceType = Class.forName(instanceTypeName);
                asset = engine.getLoaderManager().load(assetConfig, instanceType, engine.newContext());
            } else {
                asset = engine.getLoaderManager().load(assetConfig, assetType, engine.newContext());
            }
            addAsset(assetId, assetType, asset);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No such class error: " + e.getMessage(), e);
        }
    }

    // ----- Dump Asset ----- //
    public void dumpAssetsToFile(String path) {
        String yamlConfig = dumpAssetsToYaml();
        LoaderManager.writeFile(yamlConfig, path);
    }
    public String dumpAssetsToYaml() {
        List<Map<String, Object>> configList = new ArrayList<>();
        for (Map.Entry<Class<?>, Map<String, Object>> entry : getAllAssets().entrySet()) {
            Class<?> type = entry.getKey();
            for (Map.Entry<String, Object> assetEntry : entry.getValue().entrySet()) {
                Object asset = assetEntry.getValue();
                Map<String, Object> assetConfig = dumpAsset(asset);
                Map<String, Object> config = new LinkedHashMap<>();
                config.put("type", type.getName());
                if (asset.getClass() != type) config.put("instanceType", asset.getClass().getName());
                config.put("id", assetEntry.getKey());
                config.put("config", assetConfig);
                configList.add(config);
            }
        }
        Yaml yaml = new Yaml();
        return yaml.dumpAll(configList.iterator());
    }
    public Map<String, Object> dumpAsset(Object asset) {
        return engine.getLoaderManager().dump(asset, Map.class, engine.newContext());
    }

    @Override
    public void dispose() {
        for (Map.Entry<Class<?>, Map<String, Object>> entry : allAssets.entrySet()) {
            Disposable.disposeAll(entry.getValue());
        }
        allAssets.clear();
        cache.clear();
    }
}
