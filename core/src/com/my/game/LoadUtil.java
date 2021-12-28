package com.my.game;

import com.my.utils.world.AssetsManager;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.Scene;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

public class LoadUtil {

    public static final LoaderManager loaderManager = new LoaderManager();

    public static final AssetsManager assetsManager = new AssetsManager();

    public static void dumpSceneToFile(Scene scene, String path) {
        String yamlConfig = dumpSceneToYaml(scene);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(yamlConfig);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Scene loadSceneFromFile(String path) {
        String yamlConfig;
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String str;
            String ls = System.getProperty("line.separator");
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

    public static String dumpSceneToYaml(Scene scene) {
        Map config = dumpScene(scene);
        Yaml yaml = new Yaml();
        return yaml.dump(config);
    }

    public static Scene loadSceneFromYaml(String yamlConfig) {
        Yaml yaml = new Yaml();
        Map<String, Object> loadedConfig = yaml.loadAs(yamlConfig, Map.class);
        return loadScene(loadedConfig);
    }

    public static Map dumpScene(Scene scene) {
        return loaderManager.dump(scene, Map.class);
    }

    public static Scene loadScene(Map config) {
        return loaderManager.load(config, Scene.class);
    }
}
