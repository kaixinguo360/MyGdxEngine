package com.my.utils.world;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class AssetsManager {

    @Getter
    private final Map<Class<?>, Map<String, Object>> allAssets = new HashMap<>();

    private final World world;

    public AssetsManager(World world) {
        this.world = world;
    }

    public <T> T addAsset(String id, Class<T> type, T asset) {
        if (!allAssets.containsKey(type)) allAssets.put(type, new HashMap<>());
        Map<String, Object> assets = allAssets.get(type);
        if (assets.containsKey(id)) throw new RuntimeException("Duplicate Assets: " + id + " (" + type + ")");
        assets.put(id, asset);
        if (asset instanceof AfterAdded) ((AfterAdded) asset).afterAdded(world);
        return asset;
    }
    public <T> T removeAsset(String id, Class<T> type) {
        if (!allAssets.containsKey(type)) throw new RuntimeException("No Such Assets: " + id + " (" + type + ")");
        if (!allAssets.get(type).containsKey(id)) throw new RuntimeException("No Such Assets: " + id + " (" + type + ")");
        T removed = (T) allAssets.get(type).remove(id);
        if (removed instanceof AfterRemoved) ((AfterRemoved) removed).afterRemoved(world);
        return removed;
    }
    public <T> T getAsset(String id, Class<T> type) {
        if (!allAssets.containsKey(type)) throw new RuntimeException("No Such Assets: " + id + " (" + type + ")");
        if (!allAssets.get(type).containsKey(id)) throw new RuntimeException("No Such Assets: " + id + " (" + type + ")");
        return (T) allAssets.get(type).get(id);
    }
}
