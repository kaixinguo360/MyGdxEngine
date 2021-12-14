package com.my.game;

import com.my.utils.world.LoaderManager;
import com.my.utils.world.World;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class LoadUtil {

    public static void saveWorldToFile(MyGame.GameWorld gameWorld, String path) {
        String yamlConfig = saveWorldToYaml(gameWorld);
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(yamlConfig);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MyGame.GameWorld loadWorldFromFile(String path) {
        String yamlConfig = null;
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

    public static MyGame.GameWorld loadWorld(Map config) {
        LoaderManager loaderManager = new MyGame.GameLoaderManager();

        World world = loaderManager.load(config, World.class);
        world.update();

        return new MyGame.GameWorld(world, loaderManager);
    }

    public static String saveWorldToYaml(MyGame.GameWorld gameWorld) {
        Map config = gameWorld.loaderManager.getConfig(gameWorld.world, Map.class);
        Yaml yaml = new Yaml();
        return yaml.dumpAsMap(config);
    }

    public static MyGame.GameWorld loadWorldFromYaml(String yamlConfig) {
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
