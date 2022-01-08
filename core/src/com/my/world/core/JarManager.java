package com.my.world.core;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JarManager extends ClassLoader {

    public static final String CONFIG_FILE = "config.yml";
    public static final String CONFIG_INIT_CLASS = "initClass";
    public static final String CONFIG_INIT_METHOD = "initMethod";
    public static final String DEFAULT_INIT_METHOD = "init";

    @Getter
    protected final Map<String, ClassLoader> classLoaders = new LinkedHashMap<>();

    protected final Map<String, Class<?>> cache = new HashMap<>();

    protected final Yaml yaml = new Yaml();

    protected final Engine engine;

    JarManager(Engine engine) {
        this.engine = engine;
        this.addClassLoader("default", Thread.currentThread().getContextClassLoader());
    }

    public void loadJar(String path) {
        try {
            URL url = new URL(path);
            URLClassLoader classLoader = new URLClassLoader(new URL[] { url });
            addClassLoader(path, classLoader);
            initClassLoader(classLoader);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error!", e);
        }
    }

    protected void addClassLoader(String name, ClassLoader classLoader) {
        classLoaders.put(name, classLoader);
    }

    protected void initClassLoader(ClassLoader classLoader) {
        InputStream configResource = classLoader.getResourceAsStream(CONFIG_FILE);
        if (configResource == null) return;

        Map<String, Object> config = yaml.loadAs(new InputStreamReader(configResource), Map.class);
        String initClassName = (String) config.get(CONFIG_INIT_CLASS);
        String initMethodName = (String) config.get(CONFIG_INIT_METHOD);

        if (initClassName == null) return;
        if (initMethodName == null) initMethodName = DEFAULT_INIT_METHOD;

        try {
            Class<?> initClass = classLoader.loadClass(initClassName);
            Method initMethod = initClass.getMethod(initMethodName, Engine.class);
            initMethod.invoke(null, engine);
        } catch (ClassNotFoundException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Error!", e);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> cachedClass = cache.get(name);
        if (cachedClass != null) return cachedClass;

        for (ClassLoader classLoader : classLoaders.values()) {
            try {
                Class<?> type = classLoader.loadClass(name);
                if (type != null) {
                    cache.put(name, type);
                    return type;
                }
            } catch (ClassNotFoundException ignored) {}
        }
        throw new ClassNotFoundException(name);
    }
}
