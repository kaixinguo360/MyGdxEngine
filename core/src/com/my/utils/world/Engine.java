package com.my.utils.world;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Engine {

    public static final String CONTEXT_FIELD_NAME = "ENGINE";

    @Getter
    private final AssetsManager assetsManager;

    @Getter
    private final LoaderManager loaderManager;

    @Getter
    private final Context context;

    public Engine() {
        assetsManager = new AssetsManager();
        loaderManager = new LoaderManager();
        context = new Context(null);
        context.setEnvironment(AssetsManager.CONTEXT_FIELD_NAME, assetsManager);
        context.setEnvironment(LoaderManager.CONTEXT_FIELD_NAME, loaderManager);
        context.setEnvironment(Engine.CONTEXT_FIELD_NAME, this);
    }

    public Context newContext() {
        return this.context.newContext();
    }

    // ----- Load Scene ----- //
    public Scene loadSceneFromFile(String path) {
        String yamlConfig = readFile(path);
        return loadSceneFromYaml(yamlConfig);
    }
    public Scene loadSceneFromYaml(String yamlConfig) {
        Yaml yaml = new Yaml();
        Map<String, Object> config = yaml.loadAs(yamlConfig, Map.class);
        return loadScene(config);
    }
    public Scene loadScene(Map<String, Object> config) {
        return loaderManager.load(config, Scene.class, newContext());
    }

    // ----- Dump Scene ----- //
    public void dumpSceneToFile(Scene scene, String path) {
        String yamlConfig = dumpSceneToYaml(scene);
        writeFile(yamlConfig, path);
    }
    public String dumpSceneToYaml(Scene scene) {
        Map<String, Object> config = dumpScene(scene);
        Yaml yaml = new Yaml();
        return yaml.dump(config);
    }
    public Map<String, Object> dumpScene(Scene scene) {
        return loaderManager.dump(scene, Map.class, scene.newContext());
    }

    // ----- Load Asset ----- //
    public void loadAssetsFromFile(String path) {
        String yamlConfig = readFile(path);
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
                asset = loaderManager.load(assetConfig, instanceType, newContext());
            } else {
                asset = loaderManager.load(assetConfig, assetType, newContext());
            }
            assetsManager.addAsset(assetId, assetType, asset);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No such class error: " + e.getMessage(), e);
        }
    }

    // ----- Dump Asset ----- //
    public void dumpAssetsToFile(String path) {
        String yamlConfig = dumpAssetsToYaml();
        writeFile(yamlConfig, path);
    }
    public String dumpAssetsToYaml() {
        List<Map<String, Object>> configList = new ArrayList<>();
        for (Map.Entry<Class<?>, Map<String, Object>> entry : assetsManager.getAllAssets().entrySet()) {
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
        return loaderManager.dump(asset, Map.class, newContext());
    }

    // ----- Private ----- //
    private String readFile(String path) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String str;
            String ls = java.lang.System.getProperty("line.separator");
            while ((str = in.readLine()) != null) {
                sb.append(str);
                sb.append(ls);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void writeFile(String content, String path) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(content);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
