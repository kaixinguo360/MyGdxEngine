package com.my.utils.world;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
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
        String yamlConfig;
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String str;
            String ls = java.lang.System.getProperty("line.separator");
            while ((str = in.readLine()) != null) {
                sb.append(str);
                sb.append(ls);
            }
            yamlConfig = sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(yamlConfig);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String dumpSceneToYaml(Scene scene) {
        Map<String, Object> config = dumpScene(scene);
        Yaml yaml = new Yaml();
        return yaml.dump(config);
    }
    public Map<String, Object> dumpScene(Scene scene) {
        return loaderManager.dump(scene, Map.class, scene.newContext());
    }
}
