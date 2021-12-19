package com.my.utils.world;

import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AssetsManager {

    @Getter
    private final Map<Class<?>, Map<String, Object>> allAssets = new LinkedHashMap<>();

    private final Map<String, String> cache = new HashMap<>();

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
}
