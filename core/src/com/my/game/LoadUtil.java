package com.my.game;

import com.my.utils.world.AssetsManager;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.World;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

public class LoadUtil {

    public static final LoaderManager loaderManager = new LoaderManager();

    public static final AssetsManager assetsManager = new AssetsManager();

    public static void dumpWorldToFile(World world, String path) {
        String yamlConfig = dumpWorldToYaml(world);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(yamlConfig);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static World loadWorldFromFile(String path) {
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
        return loadWorldFromYaml(yamlConfig);
    }

    public static String dumpWorldToYaml(World world) {
        Map config = dumpWorld(world);
        Yaml yaml = new Yaml();
        return yaml.dump(config);
    }

    public static World loadWorldFromYaml(String yamlConfig) {
        Yaml yaml = new Yaml();
        Map<String, Object> loadedConfig = yaml.loadAs(yamlConfig, Map.class);
        return loadWorld(loadedConfig);
    }

    public static Map dumpWorld(World world) {
        return loaderManager.dump(world, Map.class);
    }

    public static World loadWorld(Map config) {
        return loaderManager.load(config, World.class);
    }
}
