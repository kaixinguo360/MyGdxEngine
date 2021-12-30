package com.my.world.core;

import lombok.Getter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoaderManager {

    public static final String CONTEXT_FIELD_NAME = "LOADER_MANAGER";

    private final Engine engine;

    @Getter
    protected final List<Loader> loaders = new ArrayList<>();
    private final Map<String, Loader> loaderCache = new HashMap<>();

    LoaderManager(Engine engine) {
        this.engine = engine;
    }

    public <E, T> T load(E config, Class<T> type, Context context) {
        if (config == null) {
            throw new RuntimeException("No such loader: null -> " + type);
        }
        String hash = config.getClass() + " -> " + type;
        if (loaderCache.containsKey(hash)) {
            return loaderCache.get(hash).load(config, type, context);
        } else {
            for (Loader loader : loaders) {
                if (loader.handleable(config.getClass(), type)) {
                    loaderCache.put(hash, loader);
                    return loader.load(config, type, context);
                }
            }
        }
        throw new RuntimeException("No such loader: " + config.getClass() + " -> " + type);
    }

    public <E, T> E dump(T obj, Class<E> configType, Context context) {
        String hash = configType + " -> " + obj.getClass();
        if (loaderCache.containsKey(hash)) {
            return loaderCache.get(hash).dump(obj, configType, context);
        } else {
            for (Loader loader : loaders) {
                if (loader.handleable(configType, obj.getClass())) {
                    loaderCache.put(hash, loader);
                    return loader.dump(obj, configType, context);
                }
            }
        }
        throw new RuntimeException("No such loader to get config: " + configType + " -> " + obj.getClass());
    }

    public <T extends Loader> T findLoader(Class<T> type) {
        for (Loader loader : loaders) {
            if (type.isInstance(loader)) {
                return (T) loader;
            }
        }
        return null;
    }

    // ----- File Utils ----- //

    public static String readFile(String path) {
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

    public static void writeFile(String content, String path) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(path));
            out.write(content);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
