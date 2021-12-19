package com.my.game;

import com.my.game.builder.WorldBuilder;
import com.my.utils.world.LoaderManager;
import com.my.utils.world.World;
import com.my.utils.world.loader.WorldLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class LoadUtil {

    public static final LoaderManager loaderManager;

    static {
        loaderManager = new LoaderManager();
        loaderManager.getLoader(WorldLoader.class).setBeforeLoadAssets(WorldBuilder::initAssets);
    }

    public static void saveWorldToFile(World world, String path) {
        String yamlConfig = saveWorldToYaml(world);
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

    public static World loadWorld(Map config) {
        return loaderManager.load(config, World.class);
    }

    public static String saveWorldToYaml(World world) {
        Map config = loaderManager.getConfig(world, Map.class);
        Yaml yaml = new Yaml();
        return yaml.dumpAsMap(config);
    }

    public static World loadWorldFromYaml(String yamlConfig) {
        Yaml yaml = new Yaml();
        Map<String, Object> loadedConfig = yaml.loadAs(yamlConfig, Map.class);
        new Consumer<Map<String, Object>>() {
            public void accept(Map<String, Object> map) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Double) {
                        entry.setValue((float) (double) value);
                    } else if (value instanceof Map) {
                        accept((Map<String, Object>) value);
                    } else if (value instanceof List) {
                        accept((List<Object>) value);
                    }
                }
            }
            public void accept(List<Object> list) {
                for (int i = 0; i < list.size(); i++) {
                    Object o = list.get(i);
                    if (o instanceof Double) {
                        list.set(i, (float) (double) o);
                    } else if (o instanceof Map) {
                        accept((Map<String, Object>) o);
                    } else if (o instanceof List) {
                        accept((List<Object>) o);
                    }
                }
            }
        }.accept(loadedConfig);
        return loadWorld(loadedConfig);
    }
}
